package com.jeuxwebapi.services;

import Entities.Stage;
import Entities.StageType;
import Entities.Tournament;
import com.jeuxwebapi.models.StageUpdateDto;
import com.jeuxwebapi.models.StageDto;
import com.jeuxwebapi.models.TournamentCreateDto;
import com.jeuxwebapi.models.TournamentDto;
import com.jeuxwebapi.models.TournamentUpdateDto;
import com.jeuxwebapi.results.ServiceDataResult;
import com.jeuxwebapi.results.ServiceListResult;
import com.jeuxwebapi.util.QueryUtils;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class TournamentService {
    @Inject
    Mutiny.SessionFactory sessionFactory;

    public Uni<ServiceListResult<TournamentDto>> findTournaments(Long leagueId, Integer season, Integer skip, Integer size, String order) {
        return sessionFactory.withSession(session -> {
            CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
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

            Mutiny.SelectionQuery<Tournament> query = session.createQuery(cq);
            QueryUtils.applyPaging(query, skip, size);

            return query.getResultList()
                    .chain(tournaments -> countTournaments(session, leagueId, season)
                            .chain(total -> loadStagesByTournament(session, tournaments)
                                    .map(stagesByTournament -> {
                                        List<TournamentDto> items = tournaments.stream()
                                                .map(tournament -> toDto(tournament, stagesByTournament))
                                                .toList();
                                        ServiceListResult<TournamentDto> result = new ServiceListResult<>();
                                        result.setResult(true);
                                        result.setItems(items);
                                        result.setTotal(total);
                                        return result;
                                    })));
        });
    }

    public Uni<ServiceDataResult<TournamentDto>> findTournamentById(long id) {
        return sessionFactory.withSession(session -> session.find(Tournament.class, id)
                .chain(tournament -> {
                    ServiceDataResult<TournamentDto> result = new ServiceDataResult<>();
                    if (tournament == null) {
                        result.setResult(false);
                        result.setMessage(String.format("Сущность 'Tournament' с id: %d не найдена!", id));
                        return Uni.createFrom().item(result);
                    }
                    return loadStagesByTournament(session, List.of(tournament))
                            .map(stagesByTournament -> {
                                result.setResult(true);
                                result.setData(toDto(tournament, stagesByTournament));
                                return result;
                            });
                }));
    }

    public Uni<ServiceDataResult<TournamentDto>> createTournament(TournamentCreateDto createDto) {
        return sessionFactory.withTransaction((session, tx) -> {
            Tournament tournament = new Tournament();
            tournament.setName(createDto.getName());
            tournament.setStYear(createDto.getStYear());
            tournament.setFnYear(createDto.getFnYear());
            tournament.setLeagueId(createDto.getLeagueId());
            return session.persist(tournament)
                    .chain(session::flush)
                    .chain(() -> upsertStages(session, tournament, createDto.getStages()))
                    .chain(() -> loadStagesByTournament(session, List.of(tournament)))
                    .map(stagesByTournament -> {
                        ServiceDataResult<TournamentDto> result = new ServiceDataResult<>();
                        result.setResult(true);
                        result.setData(toDto(tournament, stagesByTournament));
                        return result;
                    });
        });
    }

    public Uni<ServiceDataResult<TournamentDto>> updateTournament(TournamentUpdateDto updateDto) {
        return sessionFactory.withTransaction((session, tx) -> session.find(Tournament.class, updateDto.getId())
                .chain(tournament -> {
                    ServiceDataResult<TournamentDto> result = new ServiceDataResult<>();
                    if (tournament == null) {
                        result.setResult(false);
                        result.setMessage(String.format("Сущность 'Tournament' с id: %d не найдена!", updateDto.getId()));
                        return Uni.createFrom().item(result);
                    }
                    tournament.setName(updateDto.getName());
                    tournament.setStYear(updateDto.getStYear());
                    tournament.setFnYear(updateDto.getFnYear());
                    tournament.setLeagueId(updateDto.getLeagueId());
                    return upsertStages(session, tournament, updateDto.getStages())
                            .chain(() -> loadStagesByTournament(session, List.of(tournament)))
                            .map(stagesByTournament -> {
                                result.setResult(true);
                                result.setData(toDto(tournament, stagesByTournament));
                                return result;
                            });
                }));
    }

    public Uni<ServiceDataResult<TournamentDto>> deleteTournament(long id) {
        return sessionFactory.withTransaction((session, tx) -> session.find(Tournament.class, id)
                .chain(tournament -> {
                    ServiceDataResult<TournamentDto> result = new ServiceDataResult<>();
                    if (tournament == null) {
                        result.setResult(false);
                        result.setMessage(String.format("Сущность 'Tournament' с id: %d не найдена!", id));
                        return Uni.createFrom().item(result);
                    }
                    return loadStagesByTournament(session, List.of(tournament))
                            .chain(stagesByTournament -> {
                                TournamentDto dto = toDto(tournament, stagesByTournament);
                                return session.remove(tournament)
                                        .chain(session::flush)
                                        .replaceWith(() -> {
                                            result.setResult(true);
                                            result.setData(dto);
                                            return result;
                                        });
                            });
                }));
    }

    private Uni<Integer> countTournaments(Mutiny.Session session, Long leagueId, Integer season) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Tournament> root = cq.from(Tournament.class);
        List<Predicate> predicates = buildPredicates(cb, root, leagueId, season);
        cq.select(cb.count(root));
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }
        return session.createQuery(cq).getSingleResult()
                .map(total -> total == null ? 0 : total.intValue());
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

    private Uni<Map<Long, List<StageDto>>> loadStagesByTournament(Mutiny.Session session, List<Tournament> tournaments) {
        if (tournaments.isEmpty()) {
            return Uni.createFrom().item(Map.of());
        }
        List<Long> ids = tournaments.stream()
                .map(Tournament::getId)
                .toList();

        Mutiny.SelectionQuery<Stage> stageQuery = session.createQuery(
                "from Stage s where s.TournamentId in :ids order by s.Order asc, s.Id asc",
                Stage.class
        );
        stageQuery.setParameter("ids", ids);

        return stageQuery.getResultList()
                .map(items -> items.stream()
                        .collect(Collectors.groupingBy(
                                Stage::getTournamentId,
                                Collectors.mapping(TournamentService::toStageDto, Collectors.toList())
                        )));
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
                stage.getTournamentId(),
                toApiStageType(stage.getStageType())
        );
    }

    private Uni<Void> upsertStages(Mutiny.Session session, Tournament tournament, List<StageUpdateDto> stageDtos) {
        if (stageDtos == null || stageDtos.isEmpty()) {
            return session.flush().replaceWithVoid();
        }

        List<Long> stageIds = stageDtos.stream()
                .map(StageUpdateDto::getId)
                .filter(id -> id > 0)
                .toList();

        return loadExistingStages(session, tournament.getId(), stageIds)
                .chain(existingById -> {
                    List<Stage> newStages = new ArrayList<>();
                    for (StageUpdateDto stageDto : stageDtos) {
                        Stage stage = existingById.get(stageDto.getId());
                        if (stage == null) {
                            stage = new Stage();
                            newStages.add(stage);
                        }

                        stage.setName(stageDto.getName());
                        stage.setOrder(stageDto.getOrder());
                        stage.setLeagueId(stageDto.getLeagueId() == null ? tournament.getLeagueId() : stageDto.getLeagueId());
                        stage.setTournamentId(tournament.getId());
                        stage.setStageType(toEntityStageType(stageDto.getStageType()));
                    }

                    Uni<Void> persistChain = Uni.createFrom().voidItem();
                    for (Stage stage : newStages) {
                        persistChain = persistChain.chain(() -> session.persist(stage).replaceWithVoid());
                    }
                    return persistChain.chain(session::flush).replaceWithVoid();
                });
    }

    private Uni<Map<Long, Stage>> loadExistingStages(Mutiny.Session session, long tournamentId, List<Long> stageIds) {
        if (stageIds.isEmpty()) {
            return Uni.createFrom().item(Map.of());
        }

        Mutiny.SelectionQuery<Stage> query = session.createQuery(
                "from Stage s where s.TournamentId = :tournamentId and s.Id in :ids",
                Stage.class
        );
        query.setParameter("tournamentId", tournamentId);
        query.setParameter("ids", stageIds);
        return query.getResultList()
                .map(items -> items.stream()
                        .collect(Collectors.toMap(Stage::getId, stage -> stage)));
    }

    private static StageType toEntityStageType(com.jeuxwebapi.models.StageType stageType) {
        if (stageType == null) {
            return StageType.REGULAR;
        }
        return StageType.valueOf(stageType.name());
    }

    private static com.jeuxwebapi.models.StageType toApiStageType(StageType stageType) {
        if (stageType == null) {
            return com.jeuxwebapi.models.StageType.REGULAR;
        }
        return com.jeuxwebapi.models.StageType.valueOf(stageType.name());
    }
}
