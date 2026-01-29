package com.jeuxwebapi.resources;

import Entities.Stage;
import Entities.Tournament;
import com.jeuxwebapi.models.StageDto;
import com.jeuxwebapi.models.TournamentDto;
import com.jeuxwebapi.util.QueryUtils;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/tournaments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TournamentResource {
    @Inject
    EntityManager entityManager;

    @GET
    public List<TournamentDto> getTournaments(
            @QueryParam("season") Integer season,
            @QueryParam("skip") Integer skip,
            @QueryParam("size") Integer size,
            @QueryParam("order") String order
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tournament> cq = cb.createQuery(Tournament.class);
        Root<Tournament> root = cq.from(Tournament.class);

        List<Predicate> predicates = new ArrayList<>();
        if (season != null) {
            predicates.add(cb.or(
                    cb.equal(root.get("StYear"), season),
                    cb.equal(root.get("FnYear"), season)
            ));
        }
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

        return tournaments.stream()
                .map(tournament -> toDto(tournament, stagesByTournament))
                .toList();
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
                        Collectors.mapping(TournamentResource::toStageDto, Collectors.toList())
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
