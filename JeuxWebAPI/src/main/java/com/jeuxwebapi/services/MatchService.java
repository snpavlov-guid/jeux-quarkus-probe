package com.jeuxwebapi.services;

import Entities.Match;
import Entities.Team;
import com.jeuxwebapi.models.MatchCreateDto;
import com.jeuxwebapi.models.MatchDto;
import com.jeuxwebapi.models.MatchUpdateDto;
import com.jeuxwebapi.results.ServiceDataResult;
import com.jeuxwebapi.results.ServiceListResult;
import com.jeuxwebapi.results.Validation;
import com.jeuxwebapi.services.validations.ApplicationScopedDBContextInfoService;
import com.jeuxwebapi.services.validations.StringLengthValidation;
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
import java.util.Map;
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class MatchService {
    @Inject
    Mutiny.SessionFactory sessionFactory;
    @Inject
    ApplicationScopedDBContextInfoService dbContextInfoService;

    public Uni<ServiceListResult<MatchDto>> findMatches(
            Long leagueId,
            Long tournamentId,
            Long stageId,
            String tgroup,
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
                    tgroup,
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
                    .chain(items -> countMatches(
                                    session,
                                    leagueId,
                                    tournamentId,
                                    stageId,
                                    tgroup,
                                    hTeamName,
                                    gTeamName,
                                    tours,
                                    date
                            )
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
        ServiceDataResult<MatchDto> validation = validateMatchCreateUpdate(createDto);
        if (validation != null) {
            return Uni.createFrom().item(validation);
        }
        return sessionFactory.withTransaction((session, tx) -> {
            Match match = new Match();
            match.setTour(createDto.getTour());
            match.setRound(createDto.getRound());
            match.setDate(createDto.getDate());
            match.setHScore(createDto.getHScore());
            match.setGScore(createDto.getGScore());
            match.setCity(createDto.getCity());
            match.setStadium(createDto.getStadium());
            match.setGroup(createDto.getGroup());
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
        ServiceDataResult<MatchDto> validation = validateMatchCreateUpdate(updateDto);
        if (validation != null) {
            return Uni.createFrom().item(validation);
        }
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
                    match.setGroup(updateDto.getGroup());
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

    private ServiceDataResult<MatchDto> validateMatchCreateUpdate(MatchCreateDto dto) {
        List<Validation> validations = new ArrayList<>();
        Map<String, Integer> lengths = dbContextInfoService.getStringFieldLengths(Match.class);
        StringLengthValidation.addIfTooLong(validations, "round", dto.getRound(), lengths, "Round");
        StringLengthValidation.addIfTooLong(validations, "city", dto.getCity(), lengths, "City");
        StringLengthValidation.addIfTooLong(validations, "stadium", dto.getStadium(), lengths, "Stadium");
        StringLengthValidation.addIfTooLong(validations, "group", dto.getGroup(), lengths, "Group");
        return validations.isEmpty() ? null : StringLengthValidation.failure(validations);
    }

    private Uni<Integer> countMatches(
            Mutiny.Session session,
            Long leagueId,
            Long tournamentId,
            Long stageId,
            String tgroup,
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
                tgroup,
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
            String tgroup,
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
        if (tgroup != null && !tgroup.isBlank()) {
            predicates.add(cb.equal(cb.lower(root.get("Group")), tgroup.toLowerCase()));
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
                match.getGroup(),
                match.getLeagueId(),
                match.getTournamentId(),
                match.getStageId(),
                match.getHTeamId(),
                match.getGTeamId()
        );
    }
}
