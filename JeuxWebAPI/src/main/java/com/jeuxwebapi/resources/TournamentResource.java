package com.jeuxwebapi.resources;

import Entities.Tournament;
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

        return query.getResultList()
                .stream()
                .map(TournamentResource::toDto)
                .toList();
    }

    private static TournamentDto toDto(Tournament tournament) {
        return new TournamentDto(
                tournament.getId(),
                tournament.getName(),
                tournament.getStYear(),
                tournament.getFnYear(),
                tournament.getLeagueId(),
                tournament.getSeasonLabel()
        );
    }
}
