package com.jeuxwebapi.services;

import Entities.League;
import com.jeuxwebapi.models.LeagueDto;
import com.jeuxwebapi.util.QueryUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class LeagueService {
    private final EntityManager entityManager;

    public LeagueService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<LeagueDto> findLeagues(String name, Integer skip, Integer size, String order) {
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
                .map(LeagueService::toDto)
                .toList();
    }

    private static LeagueDto toDto(League league) {
        return new LeagueDto(league.getId(), league.getName());
    }
}
