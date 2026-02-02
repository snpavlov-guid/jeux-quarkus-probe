package com.jeuxwebapi.resources;

import com.jeuxwebapi.models.LeagueDto;
import com.jeuxwebapi.services.LeagueService;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/leagues")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LeagueResource {
    @Inject
    EntityManager entityManager;

    @GET
    public List<LeagueDto> getLeagues(
            @QueryParam("name") String name,
            @QueryParam("skip") Integer skip,
            @QueryParam("size") Integer size,
            @QueryParam("order") String order
    ) {
        String nameFilter = (name != null && !name.isBlank()) ? name : null;
        return new LeagueService(entityManager).findLeagues(nameFilter, skip, size, order);
    }
}
