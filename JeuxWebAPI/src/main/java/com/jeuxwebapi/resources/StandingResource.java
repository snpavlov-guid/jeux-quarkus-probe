package com.jeuxwebapi.resources;

import com.jeuxwebapi.models.StandingDto;
import com.jeuxwebapi.results.ServiceListResult;
import com.jeuxwebapi.services.StandingService;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/standings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class StandingResource {
    @Inject
    StandingService standingService;

    @GET
    public Uni<ServiceListResult<StandingDto>> getStandings(
            @QueryParam("leagueId") Long leagueId,
            @QueryParam("tournamentId") Long tournamentId,
            @QueryParam("stageId") Long stageId
    ) {
        return standingService.getStandings(leagueId, tournamentId, stageId);
    }
}
