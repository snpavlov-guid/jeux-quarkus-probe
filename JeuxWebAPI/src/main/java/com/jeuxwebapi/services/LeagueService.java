package com.jeuxwebapi.services;

import Entities.League;
import com.jeuxwebapi.models.LeagueCreateDto;
import com.jeuxwebapi.models.LeagueDto;
import com.jeuxwebapi.models.LeagueUpdateDto;
import com.jeuxwebapi.results.ServiceDataResult;
import com.jeuxwebapi.results.ServiceListResult;
import com.jeuxwebapi.results.Validation;
import com.jeuxwebapi.services.validations.ApplicationScopedDBContextInfoService;
import com.jeuxwebapi.services.validations.StringLengthValidation;
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
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class LeagueService {
    @Inject
    Mutiny.SessionFactory sessionFactory;
    @Inject
    ApplicationScopedDBContextInfoService dbContextInfoService;

    public Uni<ServiceListResult<LeagueDto>> findLeagues(String name, Integer skip, Integer size, String order) {
        return sessionFactory.withSession(session -> {
            CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
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

            Mutiny.SelectionQuery<League> query = session.createQuery(cq);
            QueryUtils.applyPaging(query, skip, size);

            return query.getResultList()
                    .map(items -> items.stream().map(LeagueService::toDto).toList())
                    .chain(items -> countLeagues(session, name)
                            .map(total -> {
                                ServiceListResult<LeagueDto> result = new ServiceListResult<>();
                                result.setResult(true);
                                result.setItems(items);
                                result.setTotal(total);
                                return result;
                            }));
        });
    }

    public Uni<ServiceDataResult<LeagueDto>> findLeagueById(long id) {
        return sessionFactory.withSession(session -> session.find(League.class, id)
                .map(league -> {
                    ServiceDataResult<LeagueDto> result = new ServiceDataResult<>();
                    if (league == null) {
                        result.setResult(false);
                        result.setMessage(String.format("Сущность 'League' с id: %d не найдена!", id));
                        return result;
                    }
                    result.setResult(true);
                    result.setData(toDto(league));
                    return result;
                }));
    }

    public Uni<ServiceDataResult<LeagueDto>> createLeague(LeagueCreateDto createDto) {
        ServiceDataResult<LeagueDto> validation = validateLeagueCreateUpdate(createDto);
        if (validation != null) {
            return Uni.createFrom().item(validation);
        }
        return sessionFactory.withTransaction((session, tx) -> {
            League league = new League();
            league.setName(createDto.getName());
            return session.persist(league)
                    .chain(session::flush)
                    .replaceWith(() -> {
                        ServiceDataResult<LeagueDto> result = new ServiceDataResult<>();
                        result.setResult(true);
                        result.setData(toDto(league));
                        return result;
                    });
        });
    }

    public Uni<ServiceDataResult<LeagueDto>> updateLeague(LeagueUpdateDto updateDto) {
        ServiceDataResult<LeagueDto> validation = validateLeagueCreateUpdate(updateDto);
        if (validation != null) {
            return Uni.createFrom().item(validation);
        }
        return sessionFactory.withTransaction((session, tx) -> session.find(League.class, updateDto.getId())
                .chain(league -> {
                    ServiceDataResult<LeagueDto> result = new ServiceDataResult<>();
                    if (league == null) {
                        result.setResult(false);
                        result.setMessage(String.format("Сущность 'League' с id: %d не найдена!", updateDto.getId()));
                        return Uni.createFrom().item(result);
                    }
                    league.setName(updateDto.getName());
                    return session.flush()
                            .replaceWith(() -> {
                                result.setResult(true);
                                result.setData(toDto(league));
                                return result;
                            });
                }));
    }

    public Uni<ServiceDataResult<LeagueDto>> deleteLeague(long id) {
        return sessionFactory.withTransaction((session, tx) -> session.find(League.class, id)
                .chain(league -> {
                    ServiceDataResult<LeagueDto> result = new ServiceDataResult<>();
                    if (league == null) {
                        result.setResult(false);
                        result.setMessage(String.format("Сущность 'League' с id: %d не найдена!", id));
                        return Uni.createFrom().item(result);
                    }
                    LeagueDto dto = toDto(league);
                    return session.remove(league)
                            .chain(session::flush)
                            .replaceWith(() -> {
                                result.setResult(true);
                                result.setData(dto);
                                return result;
                            });
                }));
    }

    private Uni<Integer> countLeagues(Mutiny.Session session, String name) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<League> root = cq.from(League.class);
        List<Predicate> predicates = buildPredicates(cb, root, name);
        cq.select(cb.count(root));
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }
        return session.createQuery(cq).getSingleResult()
                .map(total -> total == null ? 0 : total.intValue());
    }

    private ServiceDataResult<LeagueDto> validateLeagueCreateUpdate(LeagueCreateDto dto) {
        List<Validation> validations = new ArrayList<>();
        Map<String, Integer> lengths = dbContextInfoService.getStringFieldLengths(League.class);
        StringLengthValidation.addIfTooLong(validations, "name", dto.getName(), lengths, "Name");
        return validations.isEmpty() ? null : StringLengthValidation.failure(validations);
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
