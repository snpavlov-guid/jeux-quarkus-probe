package com.jeuxwebapi.resources;

import Entities.League;
import com.jeuxwebapi.models.LeagueDto;
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

@Path("/leagues")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LeagueResource {
    @Inject
    EntityManager entityManager;

    @GET
    public List<LeagueDto> getLeagues(
            @QueryParam("name") String name,
            @QueryParam("skip") Integer skip,
            @QueryParam("size") Integer size,
            @QueryParam("order") String order
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<League> cq = cb.createQuery(League.class);
        Root<League> root = cq.from(League.class);

        List<Predicate> predicates = new ArrayList<>();
        if (name != null && !name.isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("Name")), "%" + name.toLowerCase() + "%"));
        }
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        if (QueryUtils.isDesc(order)) {
            cq.orderBy(cb.desc(root.get("Name")), cb.asc(root.get("Id")));
        } else {
            cq.orderBy(cb.asc(root.get("Name")), cb.asc(root.get("Id")));
        }

        TypedQuery<League> query = entityManager.createQuery(cq);
        QueryUtils.applyPaging(query, skip, size);

        return query.getResultList()
                .stream()
                .map(LeagueResource::toDto)
                .toList();
    }

    private static LeagueDto toDto(League league) {
        return new LeagueDto(league.getId(), league.getName());
    }
}
