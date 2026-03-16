package com.jeuxwebapi.services;

import Enums.PrevPlaysType;
import Entities.Tournament;
import com.jeuxwebapi.models.StandingDto;
import com.jeuxwebapi.models.StandingMatchType;
import com.jeuxwebapi.models.TournamentDto;
import com.jeuxwebapi.services.arrange_strategies.ArrangeStrategyFactory;
import com.jeuxwebapi.services.arrange_strategies.IArrangeStrategy;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Tuple;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class ArrangeStandingService {
    private static final String STANDINGS_SQL_TEMPLATE = """
            select *
            from football."getTournamentStandingsEx"(
                :leagueId,
                :tournamentId,
                :stageId,
                %s,
                :teamids,
                %s,
                %s,
                %s
            )
            """;

    @Inject
    Mutiny.SessionFactory sessionFactory;

    public Uni<List<StandingDto>> ArrangeStandings(
            Long leagueId,
            Long tournamentId,
            Long stageId,
            String tgroup,
            StandingMatchType matchType,
            Long prevStageId,
            PrevPlaysType prevPlays,
            List<StandingDto> standings
    ) {
        if (standings == null) {
            return Uni.createFrom().nullItem();
        }

        StandingMatchType effectiveMatchType = matchType == null ? StandingMatchType.ALL : matchType;
        return resolveStrategy(tournamentId, effectiveMatchType)
                .chain(strategy -> applySubOrder(
                        leagueId,
                        tournamentId,
                        stageId,
                        tgroup,
                        effectiveMatchType,
                        prevStageId,
                        prevPlays,
                        standings,
                        strategy
                ).map(result -> applyMainOrder(strategy, result)));
    }

    private Uni<List<StandingDto>> applySubOrder(
            Long leagueId,
            Long tournamentId,
            Long stageId,
            String tgroup,
            StandingMatchType matchType,
            Long prevStageId,
            PrevPlaysType prevPlays,
            List<StandingDto> standings,
            IArrangeStrategy<StandingDto> strategy
    ) {
        Function<StandingDto, ?> groupKeyFunction = strategy.getGroupKeyFunction();
        Map<Object, List<StandingDto>> groupedStandings = standings.stream()
                .collect(Collectors.groupingBy(
                        groupKeyFunction,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        groupedStandings.values().stream()
                .filter(group -> group.size() == 1)
                .flatMap(List::stream)
                .forEach(item -> item.setSubOrder(0));

        List<Uni<Void>> multiTeamGroupTasks = groupedStandings.values().stream()
                .filter(group -> group.size() > 1)
                .map(group -> applySubOrderForGroup(
                        leagueId,
                        tournamentId,
                        stageId,
                        tgroup,
                        matchType,
                        prevStageId,
                        prevPlays,
                        group,
                        strategy
                ))
                .toList();

        if (multiTeamGroupTasks.isEmpty()) {
            return Uni.createFrom().item(standings);
        }

        return Uni.combine().all().unis(multiTeamGroupTasks)
                .discardItems()
                .replaceWith(standings);
    }

    private Uni<Void> applySubOrderForGroup(
            Long leagueId,
            Long tournamentId,
            Long stageId,
            String tgroup,
            StandingMatchType matchType,
            Long prevStageId,
            PrevPlaysType prevPlays,
            List<StandingDto> standingsGroup,
            IArrangeStrategy<StandingDto> strategy
    ) {
        Integer matchTypeCode = toMatchTypeCode(matchType);
        Integer prevPlaysCode = toPrevPlaysCode(prevPlays);
        String standingsSql = STANDINGS_SQL_TEMPLATE.formatted(
                optionalParam("tgroup", tgroup),
                optionalParam("matchtype", matchTypeCode),
                optionalParam("prevstageid", prevStageId),
                optionalParam("prevplays", prevPlaysCode)
        );
        Long[] teamIds = standingsGroup.stream()
                .map(StandingDto::getTeamId)
                .toArray(Long[]::new);

        return sessionFactory.withSession(session -> {
            var query = session.createNativeQuery(standingsSql)
                    .setParameter("leagueId", leagueId)
                    .setParameter("tournamentId", tournamentId)
                    .setParameter("stageId", stageId)
                    .setParameter("teamids", teamIds);

            if (tgroup != null) {
                query.setParameter("tgroup", tgroup);
            }
            if (matchTypeCode != null) {
                query.setParameter("matchtype", matchTypeCode);
            }
            if (prevStageId != null) {
                query.setParameter("prevstageid", prevStageId);
            }
            if (prevPlaysCode != null) {
                query.setParameter("prevplays", prevPlaysCode);
            }

            return query.getResultList();
        }).map(rows -> rows.stream()
                .map(ArrangeStandingService::toStandingDto)
                .sorted(strategy.getGroupOrderComparator())
                .toList()
        ).invoke(sortedGroup -> {
            Map<Long, Integer> subOrdersByTeamId = java.util.stream.IntStream.range(0, sortedGroup.size())
                    .boxed()
                    .collect(Collectors.toMap(
                            index -> sortedGroup.get(index).getTeamId(),
                            index -> index + 1
                    ));

            standingsGroup.stream()
                    .forEach(item -> item.setSubOrder(subOrdersByTeamId.getOrDefault(item.getTeamId(), 0)));
        }).replaceWithVoid();
    }

    private static List<StandingDto> applyMainOrder(IArrangeStrategy<StandingDto> strategy, List<StandingDto> standings) {
        List<StandingDto> sortedStandings = standings.stream()
                .sorted(strategy.getMainOrderComparator())
                .toList();

        IntStream.range(0, sortedStandings.size())
                .forEach(index -> sortedStandings.get(index).setOrder(index + 1));

        return sortedStandings;
    }

    private Uni<IArrangeStrategy<StandingDto>> resolveStrategy(Long tournamentId, StandingMatchType matchType) {
        if (tournamentId == null) {
            return Uni.createFrom().item(new ArrangeStrategyFactory<StandingDto>((TournamentDto) null, matchType).create());
        }

        return sessionFactory.withSession(session -> session.find(Tournament.class, tournamentId))
                .map(tournament -> {
                    if (tournament == null) {
                        return new ArrangeStrategyFactory<StandingDto>((TournamentDto) null, matchType).create();
                    }

                    TournamentDto tournamentDto = new TournamentDto();
                    tournamentDto.setStYear(tournament.getStYear());
                    return new ArrangeStrategyFactory<StandingDto>(tournamentDto, matchType).create();
                });
    }

    private static Integer toMatchTypeCode(StandingMatchType matchType) {
        if (matchType == null || matchType == StandingMatchType.ALL) {
            return null;
        }
        return matchType == StandingMatchType.HOME ? 1 : -1;
    }

    private static Integer toPrevPlaysCode(PrevPlaysType prevPlays) {
        if (prevPlays == null) {
            return null;
        }
        return prevPlays == PrevPlaysType.SAMETEAMS ? 1 : 0;
    }

    private static String optionalParam(String paramName, Object value) {
        return value == null ? "null" : ":" + paramName;
    }

    private static StandingDto toStandingDto(Object rowObject) {
        Object[] row;
        if (rowObject instanceof Object[] values) {
            row = values;
        } else if (rowObject instanceof Tuple tuple) {
            row = tuple.toArray();
        } else {
            throw new IllegalStateException("Unexpected standings row type: " + rowObject.getClass().getName());
        }

        return new StandingDto(
                toLong(row[0]),
                toStringValue(row[1]),
                toStringValue(row[2]),
                toInt(row[3]),
                toInt(row[4]),
                toInt(row[5]),
                toInt(row[6]),
                toInt(row[7]),
                toInt(row[8]),
                toInt(row[9]),
                toInt(row[10]),
                toInt(row[11]),
                toInt(row[12]),
                toInt(row[13]),
                toInt(row[14]),
                toInt(row[15]),
                toInt(row[16]),
                toInt(row[17]),
                toInt(row[18]),
                toInt(row[19]),
                toInt(row[20]),
                toInt(row[21]),
                toInt(row[22]),
                toInt(row[23]),
                toInt(row[24]),
                toInt(row[25]),
                toInt(row[26])
        );
    }

    private static long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private static int toInt(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
    }

    private static String toStringValue(Object value) {
        return value == null ? null : value.toString();
    }
}
