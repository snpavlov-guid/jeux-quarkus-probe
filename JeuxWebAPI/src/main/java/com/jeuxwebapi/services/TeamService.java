package com.jeuxwebapi.services;

import Entities.Team;
import com.jeuxwebapi.models.TeamCreateDto;
import com.jeuxwebapi.models.TeamDto;
import com.jeuxwebapi.models.TeamUpdateDto;
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
public class TeamService {
    @Inject
    Mutiny.SessionFactory sessionFactory;
    @Inject
    ApplicationScopedDBContextInfoService dbContextInfoService;

    public Uni<ServiceListResult<TeamDto>> findTeams(String name, Integer skip, Integer size, String order) {
        return sessionFactory.withSession(session -> {
            CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
            CriteriaQuery<Team> cq = cb.createQuery(Team.class);
            Root<Team> root = cq.from(Team.class);

            List<Predicate> predicates = buildPredicates(cb, root, name);
            if (!predicates.isEmpty()) {
                cq.where(predicates.toArray(new Predicate[0]));
            }

            if (QueryUtils.isDesc(order)) {
                cq.orderBy(cb.desc(root.get("Name")), cb.asc(root.get("Id")));
            } else {
                cq.orderBy(cb.asc(root.get("Name")), cb.asc(root.get("Id")));
            }

            Mutiny.SelectionQuery<Team> query = session.createQuery(cq);
            QueryUtils.applyPaging(query, skip, size);

            return query.getResultList()
                    .map(items -> items.stream().map(TeamService::toDto).toList())
                    .chain(items -> countTeams(session, name)
                            .map(total -> {
                                ServiceListResult<TeamDto> result = new ServiceListResult<>();
                                result.setResult(true);
                                result.setItems(items);
                                result.setTotal(total);
                                return result;
                            }));
        });
    }

    public Uni<ServiceDataResult<TeamDto>> findTeamById(long id) {
        return sessionFactory.withSession(session -> session.find(Team.class, id)
                .map(team -> {
                    ServiceDataResult<TeamDto> result = new ServiceDataResult<>();
                    if (team == null) {
                        result.setResult(false);
                        result.setMessage(String.format("Сущность 'Team' с id: %d не найдена!", id));
                        return result;
                    }
                    result.setResult(true);
                    result.setData(toDto(team));
                    return result;
                }));
    }

    public Uni<ServiceDataResult<TeamDto>> createTeam(TeamCreateDto createDto) {
        ServiceDataResult<TeamDto> validation = validateTeamCreateUpdate(createDto);
        if (validation != null) {
            return Uni.createFrom().item(validation);
        }
        return sessionFactory.withTransaction((session, tx) -> {
            Team team = new Team();
            team.setName(createDto.getName());
            team.setShortName(createDto.getShortName());
            team.setCity(createDto.getCity());
            team.setLogoUrl(createDto.getLogoUrl());
            return session.persist(team)
                    .chain(session::flush)
                    .replaceWith(() -> {
                        ServiceDataResult<TeamDto> result = new ServiceDataResult<>();
                        result.setResult(true);
                        result.setData(toDto(team));
                        return result;
                    });
        });
    }

    public Uni<ServiceDataResult<TeamDto>> updateTeam(TeamUpdateDto updateDto) {
        ServiceDataResult<TeamDto> validation = validateTeamCreateUpdate(updateDto);
        if (validation != null) {
            return Uni.createFrom().item(validation);
        }
        return sessionFactory.withTransaction((session, tx) -> session.find(Team.class, updateDto.getId())
                .chain(team -> {
                    ServiceDataResult<TeamDto> result = new ServiceDataResult<>();
                    if (team == null) {
                        result.setResult(false);
                        result.setMessage(String.format("Сущность 'Team' с id: %d не найдена!", updateDto.getId()));
                        return Uni.createFrom().item(result);
                    }
                    team.setName(updateDto.getName());
                    team.setShortName(updateDto.getShortName());
                    team.setCity(updateDto.getCity());
                    team.setLogoUrl(updateDto.getLogoUrl());
                    return session.flush()
                            .replaceWith(() -> {
                                result.setResult(true);
                                result.setData(toDto(team));
                                return result;
                            });
                }));
    }

    public Uni<ServiceDataResult<TeamDto>> deleteTeam(long id) {
        return sessionFactory.withTransaction((session, tx) -> session.find(Team.class, id)
                .chain(team -> {
                    ServiceDataResult<TeamDto> result = new ServiceDataResult<>();
                    if (team == null) {
                        result.setResult(false);
                        result.setMessage(String.format("Сущность 'Team' с id: %d не найдена!", id));
                        return Uni.createFrom().item(result);
                    }
                    TeamDto dto = toDto(team);
                    return session.remove(team)
                            .chain(session::flush)
                            .replaceWith(() -> {
                                result.setResult(true);
                                result.setData(dto);
                                return result;
                            });
                }));
    }

    private Uni<Integer> countTeams(Mutiny.Session session, String name) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Team> root = cq.from(Team.class);
        List<Predicate> predicates = buildPredicates(cb, root, name);
        cq.select(cb.count(root));
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }
        return session.createQuery(cq).getSingleResult()
                .map(total -> total == null ? 0 : total.intValue());
    }

    private ServiceDataResult<TeamDto> validateTeamCreateUpdate(TeamCreateDto dto) {
        List<Validation> validations = new ArrayList<>();
        Map<String, Integer> lengths = dbContextInfoService.getStringFieldLengths(Team.class);
        StringLengthValidation.addIfTooLong(validations, "name", dto.getName(), lengths, "Name");
        StringLengthValidation.addIfTooLong(validations, "shortName", dto.getShortName(), lengths, "ShortName");
        StringLengthValidation.addIfTooLong(validations, "city", dto.getCity(), lengths, "City");
        StringLengthValidation.addIfTooLong(validations, "logoUrl", dto.getLogoUrl(), lengths, "LogoUrl");
        return validations.isEmpty() ? null : StringLengthValidation.failure(validations);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Team> root, String name) {
        List<Predicate> predicates = new ArrayList<>();
        if (name != null && !name.isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("Name")), "%" + name.toLowerCase() + "%"));
        }
        return predicates;
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
