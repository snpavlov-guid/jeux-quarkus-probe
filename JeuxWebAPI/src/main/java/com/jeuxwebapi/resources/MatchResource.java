package com.jeuxwebapi.resources;

import Entities.Match;
import Entities.Team;
import com.jeuxwebapi.models.MatchDto;
import com.jeuxwebapi.util.QueryUtils;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Path("/matches")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MatchResource {
    @Inject
    EntityManager entityManager;

    @GET
    public List<MatchDto> getMatchs(
            @QueryParam("hteam") String hTeamName,
            @QueryParam("gteam") String gTeamName,
            @QueryParam("tours") String tours,
            @QueryParam("date") String date,
            @QueryParam("skip") Integer skip,
            @QueryParam("size") Integer size,
            @QueryParam("order") String order
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Match> cq = cb.createQuery(Match.class);
        Root<Match> root = cq.from(Match.class);

        List<Predicate> predicates = new ArrayList<>();

        if (hTeamName != null && !hTeamName.isBlank()) {
            Join<Match, Team> hTeam = root.join("HTeam", JoinType.INNER);
            predicates.add(cb.like(cb.lower(hTeam.get("Name")), "%" + hTeamName.toLowerCase() + "%"));
        }
        if (gTeamName != null && !gTeamName.isBlank()) {
            Join<Match, Team> gTeam = root.join("GTeam", JoinType.INNER);
            predicates.add(cb.like(cb.lower(gTeam.get("Name")), "%" + gTeamName.toLowerCase() + "%"));
        }

        List<Integer> tourValues = QueryUtils.parseTours(tours);
        if (!tourValues.isEmpty()) {
            predicates.add(root.get("Tour").in(tourValues));
        }

        LocalDate targetDate = QueryUtils.parseDate(date);
        if (targetDate != null) {
            Expression<LocalDate> dateOnly = cb.function("date", LocalDate.class, root.get("Date"));
            predicates.add(cb.equal(dateOnly, targetDate));
        }

        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        if (QueryUtils.isDesc(order)) {
            cq.orderBy(cb.desc(root.get("Date")), cb.desc(root.get("Id")));
        } else {
            cq.orderBy(cb.asc(root.get("Date")), cb.asc(root.get("Id")));
        }

        TypedQuery<Match> query = entityManager.createQuery(cq);
        QueryUtils.applyPaging(query, skip, size);

        return query.getResultList()
                .stream()
                .map(MatchResource::toDto)
                .toList();
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
