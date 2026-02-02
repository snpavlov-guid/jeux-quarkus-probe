package com.jeuxwebapi.services;

import Entities.League;
import com.jeuxwebapi.models.LeagueCreateDto;
import com.jeuxwebapi.models.LeagueDto;
import com.jeuxwebapi.models.LeagueUpdateDto;
import com.jeuxwebapi.results.ServiceDataResult;
import com.jeuxwebapi.results.ServiceListResult;
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

    public ServiceListResult<LeagueDto> findLeagues(String name, Integer skip, Integer size, String order) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<League> cq = cb.createQuery(League.class);
        Root<League> root = cq.from(League.class);

        List<Predicate> predicates = buildPredicates(cb, root, name);
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

        List<LeagueDto> items = query.getResultList()
                .stream()
                .map(LeagueService::toDto)
                .toList();

        int total = countLeagues(name);
        ServiceListResult<LeagueDto> result = new ServiceListResult<>();
        result.setResult(true);
        result.setItems(items);
        result.setTotal(total);
        return result;
    }

    public ServiceDataResult<LeagueDto> findLeagueById(long id) {
        League league = entityManager.find(League.class, id);
        ServiceDataResult<LeagueDto> result = new ServiceDataResult<>();
        if (league == null) {
            result.setResult(false);
            result.setMessage(String.format("Сущность 'League' с id: %d не найдена!", id));
            return result;
        }
        result.setResult(true);
        result.setData(toDto(league));
        return result;
    }

    public ServiceDataResult<LeagueDto> createLeague(LeagueCreateDto createDto) {
        League league = new League();
        league.setName(createDto.getName());
        entityManager.persist(league);
        entityManager.flush();

        ServiceDataResult<LeagueDto> result = new ServiceDataResult<>();
        result.setResult(true);
        result.setData(toDto(league));
        return result;
    }

    public ServiceDataResult<LeagueDto> updateLeague(LeagueUpdateDto updateDto) {
        League league = entityManager.find(League.class, updateDto.getId());
        ServiceDataResult<LeagueDto> result = new ServiceDataResult<>();
        if (league == null) {
            result.setResult(false);
            result.setMessage(String.format("Сущность 'League' с id: %d не найдена!", updateDto.getId()));
            return result;
        }
        league.setName(updateDto.getName());
        entityManager.flush();
        result.setResult(true);
        result.setData(toDto(league));
        return result;
    }

    public ServiceDataResult<LeagueDto> deleteLeague(long id) {
        League league = entityManager.find(League.class, id);
        ServiceDataResult<LeagueDto> result = new ServiceDataResult<>();
        if (league == null) {
            result.setResult(false);
            result.setMessage(String.format("Сущность 'League' с id: %d не найдена!", id));
            return result;
        }
        LeagueDto dto = toDto(league);
        entityManager.remove(league);
        entityManager.flush();
        result.setResult(true);
        result.setData(dto);
        return result;
    }

    private int countLeagues(String name) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<League> root = cq.from(League.class);
        List<Predicate> predicates = buildPredicates(cb, root, name);
        cq.select(cb.count(root));
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }
        Long total = entityManager.createQuery(cq).getSingleResult();
        return total == null ? 0 : total.intValue();
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<League> root, String name) {
        List<Predicate> predicates = new ArrayList<>();
        if (name != null && !name.isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("Name")), "%" + name.toLowerCase() + "%"));
        }
        return predicates;
    }

    private static LeagueDto toDto(League league) {
        return new LeagueDto(league.getId(), league.getName());
    }
}
