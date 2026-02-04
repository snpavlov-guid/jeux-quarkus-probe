package com.jeuxwebapi.resources;

import com.jeuxwebapi.auth.AppRoles;
import com.jeuxwebapi.models.TeamCreateDto;
import com.jeuxwebapi.models.TeamDto;
import com.jeuxwebapi.models.TeamUpdateDto;
import com.jeuxwebapi.results.ServiceDataResult;
import com.jeuxwebapi.results.ServiceListResult;
import com.jeuxwebapi.services.TeamService;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
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
@Authenticated
public class TeamResource {
    @Inject
    TeamService teamService;

    @GET
    public Uni<ServiceListResult<TeamDto>> getTeams(
            @QueryParam("name") String name,
            @QueryParam("skip") Integer skip,
            @QueryParam("size") Integer size,
            @QueryParam("order") String order
    ) {
        String nameFilter = (name != null && !name.isBlank()) ? name : null;
        return teamService.findTeams(nameFilter, skip, size, order);
    }

    @GET
    @Path("/{id}")
    public Uni<ServiceDataResult<TeamDto>> getTeamById(@PathParam("id") long id) {
        return teamService.findTeamById(id);
    }

    @POST
    @RolesAllowed({AppRoles.AppRole_Owner, AppRoles.AppRole_Contrib})
    public Uni<ServiceDataResult<TeamDto>> createTeam(TeamCreateDto createDto) {
        return teamService.createTeam(createDto);
    }

    @POST
    @Path("/create")
    @RolesAllowed({AppRoles.AppRole_Owner, AppRoles.AppRole_Contrib})
    public Uni<ServiceDataResult<TeamDto>> createTeamAlt(TeamCreateDto createDto) {
        return teamService.createTeam(createDto);
    }

    @PUT
    @RolesAllowed({AppRoles.AppRole_Owner, AppRoles.AppRole_Contrib})
    public Uni<ServiceDataResult<TeamDto>> updateTeam(TeamUpdateDto updateDto) {
        return teamService.updateTeam(updateDto);
    }

    @POST
    @Path("/update/{id}")
    @RolesAllowed({AppRoles.AppRole_Owner, AppRoles.AppRole_Contrib})
    public Uni<ServiceDataResult<TeamDto>> updateTeamAlt(@PathParam("id") long id, TeamUpdateDto updateDto) {
        ServiceDataResult<TeamDto> idMismatch = validateUpdateId(id, updateDto);
        if (idMismatch != null) {
            return Uni.createFrom().item(idMismatch);
        }
        return teamService.updateTeam(updateDto);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({AppRoles.AppRole_Owner, AppRoles.AppRole_Contrib})
    public Uni<ServiceDataResult<TeamDto>> deleteTeam(@PathParam("id") long id) {
        return teamService.deleteTeam(id);
    }

    @POST
    @Path("/delete/{id}")
    @RolesAllowed({AppRoles.AppRole_Owner, AppRoles.AppRole_Contrib})
    public Uni<ServiceDataResult<TeamDto>> deleteTeamAlt(@PathParam("id") long id) {
        return teamService.deleteTeam(id);
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
