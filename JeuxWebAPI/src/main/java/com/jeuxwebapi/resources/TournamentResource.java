package com.jeuxwebapi.resources;

import com.jeuxwebapi.auth.AppRoles;
import com.jeuxwebapi.models.TournamentCreateDto;
import com.jeuxwebapi.models.TournamentDto;
import com.jeuxwebapi.models.TournamentUpdateDto;
import com.jeuxwebapi.results.ServiceDataResult;
import com.jeuxwebapi.results.ServiceListResult;
import com.jeuxwebapi.services.TournamentService;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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

@Path("/tournaments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class TournamentResource {
    @Inject
    TournamentService tournamentService;

    @ConfigProperty(name = "RFLeagueId")
    long rfLeagueId;

    @GET
    public Uni<ServiceListResult<TournamentDto>> getTournaments(
            @QueryParam("leagueId") Long leagueId,
            @QueryParam("season") Integer season,
            @QueryParam("skip") Integer skip,
            @QueryParam("size") Integer size,
            @QueryParam("order") String order
    ) {
        return tournamentService.findTournaments(leagueId, season, skip, size, order);
    }

    @GET
    @Path("/rpl")
    public Uni<ServiceListResult<TournamentDto>> getRPLTournaments(
            @QueryParam("season") Integer season,
            @QueryParam("skip") Integer skip,
            @QueryParam("size") Integer size,
            @QueryParam("order") String order
    ) {
        return tournamentService.findTournaments(rfLeagueId, season, skip, size, order);
    }

    @GET
    @Path("/{id}")
    public Uni<ServiceDataResult<TournamentDto>> getTournamentById(@PathParam("id") long id) {
        return tournamentService.findTournamentById(id);
    }

    @POST
    @RolesAllowed({AppRoles.AppRole_Owner, AppRoles.AppRole_Contrib})
    public Uni<ServiceDataResult<TournamentDto>> createTournament(TournamentCreateDto createDto) {
        return tournamentService.createTournament(createDto);
    }

    @POST
    @Path("/create")
    @RolesAllowed({AppRoles.AppRole_Owner, AppRoles.AppRole_Contrib})
    public Uni<ServiceDataResult<TournamentDto>> createTournamentAlt(TournamentCreateDto createDto) {
        return tournamentService.createTournament(createDto);
    }

    @PUT
    @RolesAllowed({AppRoles.AppRole_Owner, AppRoles.AppRole_Contrib})
    public Uni<ServiceDataResult<TournamentDto>> updateTournament(TournamentUpdateDto updateDto) {
        return tournamentService.updateTournament(updateDto);
    }

    @POST
    @Path("/update/{id}")
    @RolesAllowed({AppRoles.AppRole_Owner, AppRoles.AppRole_Contrib})
    public Uni<ServiceDataResult<TournamentDto>> updateTournamentAlt(@PathParam("id") long id, TournamentUpdateDto updateDto) {
        ServiceDataResult<TournamentDto> idMismatch = validateUpdateId(id, updateDto);
        if (idMismatch != null) {
            return Uni.createFrom().item(idMismatch);
        }
        return tournamentService.updateTournament(updateDto);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({AppRoles.AppRole_Owner, AppRoles.AppRole_Contrib})
    public Uni<ServiceDataResult<TournamentDto>> deleteTournament(@PathParam("id") long id) {
        return tournamentService.deleteTournament(id);
    }

    @POST
    @Path("/delete/{id}")
    @RolesAllowed({AppRoles.AppRole_Owner, AppRoles.AppRole_Contrib})
    public Uni<ServiceDataResult<TournamentDto>> deleteTournamentAlt(@PathParam("id") long id) {
        return tournamentService.deleteTournament(id);
    }
    private ServiceDataResult<TournamentDto> validateUpdateId(long id, TournamentUpdateDto updateDto) {
        if (updateDto == null || updateDto.getId() != id) {
            ServiceDataResult<TournamentDto> result = new ServiceDataResult<>();
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
