package com.jeuxwebapi.services;

import Entities.Match;
import Entities.Team;
import com.jeuxwebapi.models.MatchDto;
import com.jeuxwebapi.util.QueryUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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

public class MatchService {
    private final EntityManager entityManager;

    public MatchService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<MatchDto> findMatches(
            String hTeamName,
            String gTeamName,
            List<Integer> tours,
            LocalDate date,
            Integer skip,
            Integer size,
            String order
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

        if (tours != null && !tours.isEmpty()) {
            predicates.add(root.get("Tour").in(tours));
        }

        if (date != null) {
            Expression<LocalDate> dateOnly = cb.function("date", LocalDate.class, root.get("Date"));
            predicates.add(cb.equal(dateOnly, date));
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
                .map(MatchService::toDto)
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
