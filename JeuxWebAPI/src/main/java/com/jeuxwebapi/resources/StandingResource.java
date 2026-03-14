package com.jeuxwebapi.resources;

import Enums.PrevPlaysType;
import com.jeuxwebapi.models.StandingDto;
import com.jeuxwebapi.models.StandingMatchType;
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
            @QueryParam("stageId") Long stageId,
            @QueryParam("tgroup") String tgroup,
            @QueryParam("matchtype") StandingMatchType matchType,
            @QueryParam("prevstageid") Long prevStageId,
            @QueryParam("prevplays") String prevPlaysRaw
    ) {
        PrevPlaysType prevPlays = parsePrevPlays(prevPlaysRaw);
        if (prevPlaysRaw != null && prevPlays == null) {
            ServiceListResult<StandingDto> invalidResult = new ServiceListResult<>();
            invalidResult.setResult(false);
            invalidResult.setMessage("Параметр prevplays должен быть ALLPLAYS или SAMETEAMS.");
            invalidResult.setItems(java.util.List.of());
            invalidResult.setTotal(0);
            return Uni.createFrom().item(invalidResult);
        }

        return standingService.getStandings(
                leagueId,
                tournamentId,
                stageId,
                tgroup,
                matchType,
                prevStageId,
                prevPlays
        );
    }

    private static PrevPlaysType parsePrevPlays(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return PrevPlaysType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
