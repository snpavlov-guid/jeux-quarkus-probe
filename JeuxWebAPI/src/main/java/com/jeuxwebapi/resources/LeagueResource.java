package com.jeuxwebapi.resources;

import com.jeuxwebapi.models.LeagueCreateDto;
import com.jeuxwebapi.models.LeagueDto;
import com.jeuxwebapi.models.LeagueUpdateDto;
import com.jeuxwebapi.results.ServiceDataResult;
import com.jeuxwebapi.results.ServiceListResult;
import com.jeuxwebapi.services.LeagueService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Inject;
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

@Path("/leagues")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LeagueResource {
    @Inject
    LeagueService leagueService;

    @ConfigProperty(name = "RFLeagueId")
    long rfLeagueId;

    @GET
    public ServiceListResult<LeagueDto> getLeagues(
            @QueryParam("name") String name,
            @QueryParam("skip") Integer skip,
            @QueryParam("size") Integer size,
            @QueryParam("order") String order
    ) {
        String nameFilter = (name != null && !name.isBlank()) ? name : null;
        return leagueService.findLeagues(nameFilter, skip, size, order);
    }

    @GET
    @Path("/{id}")
    public ServiceDataResult<LeagueDto> getLeagueById(@PathParam("id") long id) {
        return leagueService.findLeagueById(id);
    }

    @GET
    @Path("/rpl")
    public ServiceDataResult<LeagueDto> getRPLeague() {
        return leagueService.findLeagueById(rfLeagueId);
    }

    @POST
    @Transactional
    public ServiceDataResult<LeagueDto> createLeague(LeagueCreateDto createDto) {
        return leagueService.createLeague(createDto);
    }

    @POST
    @Path("/create")
    @Transactional
    public ServiceDataResult<LeagueDto> createLeagueAlt(LeagueCreateDto createDto) {
        return leagueService.createLeague(createDto);
    }

    @PUT
    @Transactional
    public ServiceDataResult<LeagueDto> updateLeague(LeagueUpdateDto updateDto) {
        return leagueService.updateLeague(updateDto);
    }

    @POST
    @Path("/update/{id}")
    @Transactional
    public ServiceDataResult<LeagueDto> updateLeagueAlt(@PathParam("id") long id, LeagueUpdateDto updateDto) {
        ServiceDataResult<LeagueDto> idMismatch = validateUpdateId(id, updateDto);
        if (idMismatch != null) {
            return idMismatch;
        }
        return leagueService.updateLeague(updateDto);
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public ServiceDataResult<LeagueDto> deleteLeague(@PathParam("id") long id) {
        return leagueService.deleteLeague(id);
    }

    @POST
    @Path("/delete/{id}")
    @Transactional
    public ServiceDataResult<LeagueDto> deleteLeagueAlt(@PathParam("id") long id) {
        return leagueService.deleteLeague(id);
    }

    private ServiceDataResult<LeagueDto> validateUpdateId(long id, LeagueUpdateDto updateDto) {
        if (updateDto == null || updateDto.getId() != id) {
            ServiceDataResult<LeagueDto> result = new ServiceDataResult<>();
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
