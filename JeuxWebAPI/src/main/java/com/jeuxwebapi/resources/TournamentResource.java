package com.jeuxwebapi.resources;

import com.jeuxwebapi.models.TournamentDto;
import com.jeuxwebapi.services.TournamentService;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/tournaments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TournamentResource {
    @Inject
    EntityManager entityManager;

    @GET
    public List<TournamentDto> getTournaments(
            @QueryParam("season") Integer season,
            @QueryParam("skip") Integer skip,
            @QueryParam("size") Integer size,
            @QueryParam("order") String order
    ) {
        return new TournamentService(entityManager).findTournaments(season, skip, size, order);
    }
}
