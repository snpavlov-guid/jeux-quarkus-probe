package com.jeuxwebapi.services;

import Entities.Match;
import Entities.Team;
import com.jeuxwebapi.models.MatchCreateDto;
import com.jeuxwebapi.models.MatchDto;
import com.jeuxwebapi.models.MatchUpdateDto;
import com.jeuxwebapi.results.ServiceDataResult;
import com.jeuxwebapi.results.ServiceListResult;
import com.jeuxwebapi.util.QueryUtils;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class MatchService {
    @Inject
    Mutiny.SessionFactory sessionFactory;

    public Uni<ServiceListResult<MatchDto>> findMatches(
            Long leagueId,
            Long tournamentId,
            Long stageId,
            String hTeamName,
            String gTeamName,
            List<Integer> tours,
            LocalDate date,
            Integer skip,
            Integer size,
            String order
    ) {
        return sessionFactory.withSession(session -> {
            CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
            CriteriaQuery<Match> cq = cb.createQuery(Match.class);
            Root<Match> root = cq.from(Match.class);

            List<Predicate> predicates = buildPredicates(
                    cb,
                    root,
                    leagueId,
                    tournamentId,
                    stageId,
                    hTeamName,
                    gTeamName,
                    tours,
                    date
            );

            if (!predicates.isEmpty()) {
                cq.where(predicates.toArray(new Predicate[0]));
            }

            if (QueryUtils.isDesc(order)) {
                cq.orderBy(cb.desc(root.get("Date")), cb.desc(root.get("Id")));
            } else {
                cq.orderBy(cb.asc(root.get("Date")), cb.asc(root.get("Id")));
            }

            Mutiny.SelectionQuery<Match> query = session.createQuery(cq);
            QueryUtils.applyPaging(query, skip, size);

            return query.getResultList()
                    .map(items -> items.stream().map(MatchService::toDto).toList())
                    .chain(items -> countMatches(session, leagueId, tournamentId, stageId, hTeamName, gTeamName, tours, date)
                            .map(total -> {
                                ServiceListResult<MatchDto> result = new ServiceListResult<>();
                                result.setResult(true);
                                result.setItems(items);
                                result.setTotal(total);
                                return result;
                            }));
        });
    }

    public Uni<ServiceDataResult<MatchDto>> findMatchById(long id) {
        return sessionFactory.withSession(session -> session.find(Match.class, id)
                .map(match -> {
                    ServiceDataResult<MatchDto> result = new ServiceDataResult<>();
                    if (match == null) {
                        result.setResult(false);
                        result.setMessage(String.format("Сущность 'Match' с id: %d не найдена!", id));
                        return result;
                    }
                    result.setResult(true);
                    result.setData(toDto(match));
                    return result;
                }));
    }

    public Uni<ServiceDataResult<MatchDto>> createMatch(MatchCreateDto createDto) {
        return sessionFactory.withTransaction((session, tx) -> {
            Match match = new Match();
            match.setTour(createDto.getTour());
            match.setRound(createDto.getRound());
            match.setDate(createDto.getDate());
            match.setHScore(createDto.getHScore());
            match.setGScore(createDto.getGScore());
            match.setCity(createDto.getCity());
            match.setStadium(createDto.getStadium());
            match.setLeagueId(createDto.getLeagueId());
            match.setTournamentId(createDto.getTournamentId());
            match.setStageId(createDto.getStageId());
            match.setHTeamId(createDto.getHTeamId());
            match.setGTeamId(createDto.getGTeamId());
            return session.persist(match)
                    .chain(session::flush)
                    .replaceWith(() -> {
                        ServiceDataResult<MatchDto> result = new ServiceDataResult<>();
                        result.setResult(true);
                        result.setData(toDto(match));
                        return result;
                    });
        });
    }

    public Uni<ServiceDataResult<MatchDto>> updateMatch(MatchUpdateDto updateDto) {
        return sessionFactory.withTransaction((session, tx) -> session.find(Match.class, updateDto.getId())
                .chain(match -> {
                    ServiceDataResult<MatchDto> result = new ServiceDataResult<>();
                    if (match == null) {
                        result.setResult(false);
                        result.setMessage(String.format("Сущность 'Match' с id: %d не найдена!", updateDto.getId()));
                        return Uni.createFrom().item(result);
                    }
                    match.setTour(updateDto.getTour());
                    match.setRound(updateDto.getRound());
                    match.setDate(updateDto.getDate());
                    match.setHScore(updateDto.getHScore());
                    match.setGScore(updateDto.getGScore());
                    match.setCity(updateDto.getCity());
                    match.setStadium(updateDto.getStadium());
                    match.setLeagueId(updateDto.getLeagueId());
                    match.setTournamentId(updateDto.getTournamentId());
                    match.setStageId(updateDto.getStageId());
                    match.setHTeamId(updateDto.getHTeamId());
                    match.setGTeamId(updateDto.getGTeamId());
                    return session.flush()
                            .replaceWith(() -> {
                                result.setResult(true);
                                result.setData(toDto(match));
                                return result;
                            });
                }));
    }

    public Uni<ServiceDataResult<MatchDto>> deleteMatch(long id) {
        return sessionFactory.withTransaction((session, tx) -> session.find(Match.class, id)
                .chain(match -> {
                    ServiceDataResult<MatchDto> result = new ServiceDataResult<>();
                    if (match == null) {
                        result.setResult(false);
                        result.setMessage(String.format("Сущность 'Match' с id: %d не найдена!", id));
                        return Uni.createFrom().item(result);
                    }
                    MatchDto dto = toDto(match);
                    return session.remove(match)
                            .chain(session::flush)
                            .replaceWith(() -> {
                                result.setResult(true);
                                result.setData(dto);
                                return result;
                            });
                }));
    }

    private Uni<Integer> countMatches(
            Mutiny.Session session,
            Long leagueId,
            Long tournamentId,
            Long stageId,
            String hTeamName,
            String gTeamName,
            List<Integer> tours,
            LocalDate date
    ) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Match> root = cq.from(Match.class);
        List<Predicate> predicates = buildPredicates(
                cb,
                root,
                leagueId,
                tournamentId,
                stageId,
                hTeamName,
                gTeamName,
                tours,
                date
        );
        cq.select(cb.count(root));
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }
        return session.createQuery(cq).getSingleResult()
                .map(total -> total == null ? 0 : total.intValue());
    }

    private List<Predicate> buildPredicates(
            CriteriaBuilder cb,
            Root<Match> root,
            Long leagueId,
            Long tournamentId,
            Long stageId,
            String hTeamName,
            String gTeamName,
            List<Integer> tours,
            LocalDate date
    ) {
        List<Predicate> predicates = new ArrayList<>();

        if (leagueId != null) {
            predicates.add(cb.equal(root.get("LeagueId"), leagueId));
        }
        if (tournamentId != null) {
            predicates.add(cb.equal(root.get("TournamentId"), tournamentId));
        }
        if (stageId != null) {
            predicates.add(cb.equal(root.get("StageId"), stageId));
        }
        if (hTeamName != null && !hTeamName.isBlank()) {
            Join<Match, Team> hTeam = root.join("HTeam", JoinType.INNER);
            predicates.add(cb.like(cb.lower(hTeam.get("Name")), "%" + hTeamName.toLowerCase() + "%"));
        }
        if (gTeamName != null && !gTeamName.isBlank()) {
            Join<Match, Team> gTeam = root.join("GTeam", JoinType.INNER);
            predicates.add(cb.like(cb.lower(gTeam.get("Name")), "%" + gTeamName.toLowerCase() + "%"));
        }

        if (tours != null && !tours.isEmpty()) {
            predicates.add(root.get("Tour").in(tours));
        }

        if (date != null) {
            Expression<LocalDate> dateOnly = cb.function("date", LocalDate.class, root.get("Date"));
            predicates.add(cb.equal(dateOnly, date));
        }
        return predicates;
    }

    private static MatchDto toDto(Match match) {
        return new MatchDto(
                match.getId(),
                match.getTour(),
                match.getRound(),
                match.getDate(),
                match.getHScore(),
                match.getGScore(),
                match.getCity(),
                match.getStadium(),
                match.getLeagueId(),
                match.getTournamentId(),
                match.getStageId(),
                match.getHTeamId(),
                match.getGTeamId()
        );
    }
}
