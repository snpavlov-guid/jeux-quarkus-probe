package com.jeuxwebapi.resources;

import com.jeuxwebapi.models.MatchDto;
import com.jeuxwebapi.services.MatchService;
import com.jeuxwebapi.util.QueryUtils;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Path("/matches")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MatchResource {
    @Inject
    EntityManager entityManager;

    @GET
    public List<MatchDto> getMatchs(
            @QueryParam("hteam") String hTeamName,
            @QueryParam("gteam") String gTeamName,
            @QueryParam("tours") String tours,
            @QueryParam("date") String date,
            @QueryParam("skip") Integer skip,
            @QueryParam("size") Integer size,
            @QueryParam("order") String order
    ) {
        String homeTeam = (hTeamName != null && !hTeamName.isBlank()) ? hTeamName : null;
        String guestTeam = (gTeamName != null && !gTeamName.isBlank()) ? gTeamName : null;
        List<Integer> tourValues = QueryUtils.parseTours(tours);
        LocalDate targetDate = QueryUtils.parseDate(date);
        return new MatchService(entityManager).findMatches(
                homeTeam,
                guestTeam,
                tourValues,
                targetDate,
                skip,
                size,
                order
        );
    }
}
