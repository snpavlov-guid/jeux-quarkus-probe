package com.jeuxwebapi.services;

import Entities.Team;
import com.jeuxwebapi.models.TeamDto;
import com.jeuxwebapi.util.QueryUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class TeamService {
    private final EntityManager entityManager;

    public TeamService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<TeamDto> findTeams(String name, Integer skip, Integer size, String order) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Team> cq = cb.createQuery(Team.class);
        Root<Team> root = cq.from(Team.class);

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

        TypedQuery<Team> query = entityManager.createQuery(cq);
        QueryUtils.applyPaging(query, skip, size);

        return query.getResultList()
                .stream()
                .map(TeamService::toDto)
                .toList();
    }

    private static TeamDto toDto(Team team) {
        return new TeamDto(
                team.getId(),
                team.getName(),
                team.getShortName(),
                team.getCity(),
                team.getLogoUrl()
        );
    }
}
