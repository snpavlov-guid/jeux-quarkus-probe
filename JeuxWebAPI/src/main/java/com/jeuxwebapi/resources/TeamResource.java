package com.jeuxwebapi.resources;

import com.jeuxwebapi.models.TeamCreateDto;
import com.jeuxwebapi.models.TeamDto;
import com.jeuxwebapi.models.TeamUpdateDto;
import com.jeuxwebapi.results.ServiceDataResult;
import com.jeuxwebapi.results.ServiceListResult;
import com.jeuxwebapi.services.TeamService;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/teams")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TeamResource {
    @Inject
    EntityManager entityManager;

    @GET
    public ServiceListResult<TeamDto> getTeams(
            @QueryParam("name") String name,
            @QueryParam("skip") Integer skip,
            @QueryParam("size") Integer size,
            @QueryParam("order") String order
    ) {
        String nameFilter = (name != null && !name.isBlank()) ? name : null;
        return new TeamService(entityManager).findTeams(nameFilter, skip, size, order);
    }

    @GET
    @Path("/{id}")
    public ServiceDataResult<TeamDto> getTeamById(@PathParam("id") long id) {
        return new TeamService(entityManager).findTeamById(id);
    }

    @POST
    @Transactional
    public ServiceDataResult<TeamDto> createTeam(TeamCreateDto createDto) {
        return new TeamService(entityManager).createTeam(createDto);
    }

    @POST
    @Path("/create")
    @Transactional
    public ServiceDataResult<TeamDto> createTeamAlt(TeamCreateDto createDto) {
        return new TeamService(entityManager).createTeam(createDto);
    }

    @PUT
    @Transactional
    public ServiceDataResult<TeamDto> updateTeam(TeamUpdateDto updateDto) {
        return new TeamService(entityManager).updateTeam(updateDto);
    }

    @POST
    @Path("/update/{id}")
    @Transactional
    public ServiceDataResult<TeamDto> updateTeamAlt(@PathParam("id") long id, TeamUpdateDto updateDto) {
        ServiceDataResult<TeamDto> idMismatch = validateUpdateId(id, updateDto);
        if (idMismatch != null) {
            return idMismatch;
        }
        return new TeamService(entityManager).updateTeam(updateDto);
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public ServiceDataResult<TeamDto> deleteTeam(@PathParam("id") long id) {
        return new TeamService(entityManager).deleteTeam(id);
    }

    @POST
    @Path("/delete/{id}")
    @Transactional
    public ServiceDataResult<TeamDto> deleteTeamAlt(@PathParam("id") long id) {
        return new TeamService(entityManager).deleteTeam(id);
    }

    private ServiceDataResult<TeamDto> validateUpdateId(long id, TeamUpdateDto updateDto) {
        if (updateDto == null || updateDto.getId() != id) {
            ServiceDataResult<TeamDto> result = new ServiceDataResult<>();
            result.setResult(false);
            long dtoId = updateDto == null ? 0L : updateDto.getId();
            result.setMessage(String.format(
                    "Несовпадение идентификатора: id в пути=%d, id в теле=%d.",
                    id,
                    dtoId
            ));
            return result;
        }
        return null;
    }
}
