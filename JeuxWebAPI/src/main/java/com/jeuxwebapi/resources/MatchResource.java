package com.jeuxwebapi.resources;

import com.jeuxwebapi.auth.AppRoles;
import com.jeuxwebapi.models.MatchCreateDto;
import com.jeuxwebapi.models.MatchDto;
import com.jeuxwebapi.models.MatchUpdateDto;
import com.jeuxwebapi.results.ServiceDataResult;
import com.jeuxwebapi.results.ServiceListResult;
import com.jeuxwebapi.services.MatchService;
import com.jeuxwebapi.util.QueryUtils;
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
import java.time.LocalDate;
import java.util.List;

@Path("/matches")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class MatchResource {
    @Inject
    MatchService matchService;

    @GET
    public Uni<ServiceListResult<MatchDto>> getMatchs(
            @QueryParam("leagueId") Long leagueId,
            @QueryParam("tournamentId") Long tournamentId,
            @QueryParam("stageId") Long stageId,
            @QueryParam("tgroup") String tgroup,
            @QueryParam("hteam") String hTeamName,
            @QueryParam("gteam") String gTeamName,
            @QueryParam("tours") String tours,
            @QueryParam("date") String date,
            @QueryParam("skip") Integer skip,
            @QueryParam("size") Integer size,
            @QueryParam("order") String order
    ) {
        String tournamentGroup = (tgroup != null && !tgroup.isBlank()) ? tgroup : null;
        String homeTeam = (hTeamName != null && !hTeamName.isBlank()) ? hTeamName : null;
        String guestTeam = (gTeamName != null && !gTeamName.isBlank()) ? gTeamName : null;
        List<Integer> tourValues = QueryUtils.parseTours(tours);
        LocalDate targetDate = QueryUtils.parseDate(date);
        return matchService.findMatches(
                leagueId,
                tournamentId,
                stageId,
                tournamentGroup,
                homeTeam,
                guestTeam,
                tourValues,
                targetDate,
                skip,
                size,
                order
        );
    }

    @GET
    @Path("/{id}")
    public Uni<ServiceDataResult<MatchDto>> getMatchById(@PathParam("id") long id) {
        return matchService.findMatchById(id);
    }

    @POST
    @RolesAllowed({AppRoles.AppRole_Owner, AppRoles.AppRole_Contrib})
    public Uni<ServiceDataResult<MatchDto>> createMatch(MatchCreateDto createDto) {
        return matchService.createMatch(createDto);
    }

    @POST
    @Path("/create")
    @RolesAllowed({AppRoles.AppRole_Owner, AppRoles.AppRole_Contrib})
    public Uni<ServiceDataResult<MatchDto>> createMatchAlt(MatchCreateDto createDto) {
        return matchService.createMatch(createDto);
    }

    @PUT
    @RolesAllowed({AppRoles.AppRole_Owner, AppRoles.AppRole_Contrib})
    public Uni<ServiceDataResult<MatchDto>> updateMatch(MatchUpdateDto updateDto) {
        return matchService.updateMatch(updateDto);
    }

    @POST
    @Path("/update/{id}")
    @RolesAllowed({AppRoles.AppRole_Owner, AppRoles.AppRole_Contrib})
    public Uni<ServiceDataResult<MatchDto>> updateMatchAlt(@PathParam("id") long id, MatchUpdateDto updateDto) {
        ServiceDataResult<MatchDto> idMismatch = validateUpdateId(id, updateDto);
        if (idMismatch != null) {
            return Uni.createFrom().item(idMismatch);
        }
        return matchService.updateMatch(updateDto);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({AppRoles.AppRole_Owner, AppRoles.AppRole_Contrib})
    public Uni<ServiceDataResult<MatchDto>> deleteMatch(@PathParam("id") long id) {
        return matchService.deleteMatch(id);
    }

    @POST
    @Path("/delete/{id}")
    @RolesAllowed({AppRoles.AppRole_Owner, AppRoles.AppRole_Contrib})
    public Uni<ServiceDataResult<MatchDto>> deleteMatchAlt(@PathParam("id") long id) {
        return matchService.deleteMatch(id);
    }
    private ServiceDataResult<MatchDto> validateUpdateId(long id, MatchUpdateDto updateDto) {
        if (updateDto == null || updateDto.getId() != id) {
            ServiceDataResult<MatchDto> result = new ServiceDataResult<>();
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
