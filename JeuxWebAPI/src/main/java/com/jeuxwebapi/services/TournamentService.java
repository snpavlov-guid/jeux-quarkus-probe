package com.jeuxwebapi.services;

import Entities.Stage;
import Entities.Tournament;
import com.jeuxwebapi.models.StageDto;
import com.jeuxwebapi.models.TournamentCreateDto;
import com.jeuxwebapi.models.TournamentDto;
import com.jeuxwebapi.models.TournamentUpdateDto;
import com.jeuxwebapi.results.ServiceDataResult;
import com.jeuxwebapi.results.ServiceListResult;
import com.jeuxwebapi.util.QueryUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class TournamentService {
    @Inject
    EntityManager entityManager;

    public ServiceListResult<TournamentDto> findTournaments(Long leagueId, Integer season, Integer skip, Integer size, String order) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tournament> cq = cb.createQuery(Tournament.class);
        Root<Tournament> root = cq.from(Tournament.class);

        List<Predicate> predicates = buildPredicates(cb, root, leagueId, season);
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        if (QueryUtils.isDesc(order)) {
            cq.orderBy(cb.desc(root.get("StYear")), cb.asc(root.get("Id")));
        } else {
            cq.orderBy(cb.asc(root.get("StYear")), cb.asc(root.get("Id")));
        }

        TypedQuery<Tournament> query = entityManager.createQuery(cq);
        QueryUtils.applyPaging(query, skip, size);

        List<Tournament> tournaments = query.getResultList();
        Map<Long, List<StageDto>> stagesByTournament = loadStagesByTournament(tournaments);

        List<TournamentDto> items = tournaments.stream()
                .map(tournament -> toDto(tournament, stagesByTournament))
                .toList();

        int total = countTournaments(leagueId, season);
        ServiceListResult<TournamentDto> result = new ServiceListResult<>();
        result.setResult(true);
        result.setItems(items);
        result.setTotal(total);
        return result;
    }

    public ServiceDataResult<TournamentDto> findTournamentById(long id) {
        Tournament tournament = entityManager.find(Tournament.class, id);
        ServiceDataResult<TournamentDto> result = new ServiceDataResult<>();
        if (tournament == null) {
            result.setResult(false);
            result.setMessage(String.format("Сущность 'Tournament' с id: %d не найдена!", id));
            return result;
        }
        Map<Long, List<StageDto>> stagesByTournament = loadStagesByTournament(List.of(tournament));
        result.setResult(true);
        result.setData(toDto(tournament, stagesByTournament));
        return result;
    }

    public ServiceDataResult<TournamentDto> createTournament(TournamentCreateDto createDto) {
        Tournament tournament = new Tournament();
        tournament.setName(createDto.getName());
        tournament.setStYear(createDto.getStYear());
        tournament.setFnYear(createDto.getFnYear());
        tournament.setLeagueId(createDto.getLeagueId());
        entityManager.persist(tournament);
        entityManager.flush();

        ServiceDataResult<TournamentDto> result = new ServiceDataResult<>();
        result.setResult(true);
        result.setData(toDto(tournament, Map.of()));
        return result;
    }

    public ServiceDataResult<TournamentDto> updateTournament(TournamentUpdateDto updateDto) {
        Tournament tournament = entityManager.find(Tournament.class, updateDto.getId());
        ServiceDataResult<TournamentDto> result = new ServiceDataResult<>();
        if (tournament == null) {
            result.setResult(false);
            result.setMessage(String.format("Сущность 'Tournament' с id: %d не найдена!", updateDto.getId()));
            return result;
        }
        tournament.setName(updateDto.getName());
        tournament.setStYear(updateDto.getStYear());
        tournament.setFnYear(updateDto.getFnYear());
        tournament.setLeagueId(updateDto.getLeagueId());
        entityManager.flush();

        Map<Long, List<StageDto>> stagesByTournament = loadStagesByTournament(List.of(tournament));
        result.setResult(true);
        result.setData(toDto(tournament, stagesByTournament));
        return result;
    }

    public ServiceDataResult<TournamentDto> deleteTournament(long id) {
        Tournament tournament = entityManager.find(Tournament.class, id);
        ServiceDataResult<TournamentDto> result = new ServiceDataResult<>();
        if (tournament == null) {
            result.setResult(false);
            result.setMessage(String.format("Сущность 'Tournament' с id: %d не найдена!", id));
            return result;
        }
        Map<Long, List<StageDto>> stagesByTournament = loadStagesByTournament(List.of(tournament));
        TournamentDto dto = toDto(tournament, stagesByTournament);
        entityManager.remove(tournament);
        entityManager.flush();
        result.setResult(true);
        result.setData(dto);
        return result;
    }

    private int countTournaments(Long leagueId, Integer season) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Tournament> root = cq.from(Tournament.class);
        List<Predicate> predicates = buildPredicates(cb, root, leagueId, season);
        cq.select(cb.count(root));
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }
        Long total = entityManager.createQuery(cq).getSingleResult();
        return total == null ? 0 : total.intValue();
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Tournament> root, Long leagueId, Integer season) {
        List<Predicate> predicates = new ArrayList<>();
        if (leagueId != null) {
            predicates.add(cb.equal(root.get("LeagueId"), leagueId));
        }
        if (season != null) {
            predicates.add(cb.or(
                    cb.equal(root.get("StYear"), season),
                    cb.equal(root.get("FnYear"), season)
            ));
        }
        return predicates;
    }

    private Map<Long, List<StageDto>> loadStagesByTournament(List<Tournament> tournaments) {
        if (tournaments.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = tournaments.stream()
                .map(Tournament::getId)
                .toList();

        TypedQuery<Stage> stageQuery = entityManager.createQuery(
                "from Stage s where s.TournamentId in :ids order by s.Order asc, s.Id asc",
                Stage.class
        );
        stageQuery.setParameter("ids", ids);

        return stageQuery.getResultList().stream()
                .collect(Collectors.groupingBy(
                        Stage::getTournamentId,
                        Collectors.mapping(TournamentService::toStageDto, Collectors.toList())
                ));
    }

    private static TournamentDto toDto(Tournament tournament, Map<Long, List<StageDto>> stagesByTournament) {
        return new TournamentDto(
                tournament.getId(),
                tournament.getName(),
                tournament.getStYear(),
                tournament.getFnYear(),
                tournament.getLeagueId(),
                tournament.getSeasonLabel(),
                stagesByTournament.getOrDefault(tournament.getId(), List.of())
        );
    }

    private static StageDto toStageDto(Stage stage) {
        return new StageDto(
                stage.getId(),
                stage.getName(),
                stage.getOrder(),
                stage.getLeagueId(),
                stage.getTournamentId()
        );
    }
}
