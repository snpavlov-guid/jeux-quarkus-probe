package com.jeuxwebapi.resources;

import com.jeuxwebapi.models.MatchCreateDto;
import com.jeuxwebapi.models.MatchDto;
import com.jeuxwebapi.models.MatchUpdateDto;
import com.jeuxwebapi.results.ServiceDataResult;
import com.jeuxwebapi.results.ServiceListResult;
import com.jeuxwebapi.services.MatchService;
import com.jeuxwebapi.util.QueryUtils;
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
import java.time.LocalDate;
import java.util.List;

@Path("/matches")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MatchResource {
    @Inject
    EntityManager entityManager;

    @GET
    public ServiceListResult<MatchDto> getMatchs(
            @QueryParam("leagueId") Long leagueId,
            @QueryParam("stageId") Long stageId,
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
                leagueId,
                stageId,
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
    public ServiceDataResult<MatchDto> getMatchById(@PathParam("id") long id) {
        return new MatchService(entityManager).findMatchById(id);
    }

    @POST
    @Transactional
    public ServiceDataResult<MatchDto> createMatch(MatchCreateDto createDto) {
        return new MatchService(entityManager).createMatch(createDto);
    }

    @POST
    @Path("/create")
    @Transactional
    public ServiceDataResult<MatchDto> createMatchAlt(MatchCreateDto createDto) {
        return new MatchService(entityManager).createMatch(createDto);
    }

    @PUT
    @Transactional
    public ServiceDataResult<MatchDto> updateMatch(MatchUpdateDto updateDto) {
        return new MatchService(entityManager).updateMatch(updateDto);
    }

    @POST
    @Path("/update/{id}")
    @Transactional
    public ServiceDataResult<MatchDto> updateMatchAlt(@PathParam("id") long id, MatchUpdateDto updateDto) {
        ServiceDataResult<MatchDto> idMismatch = validateUpdateId(id, updateDto);
        if (idMismatch != null) {
            return idMismatch;
        }
        return new MatchService(entityManager).updateMatch(updateDto);
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public ServiceDataResult<MatchDto> deleteMatch(@PathParam("id") long id) {
        return new MatchService(entityManager).deleteMatch(id);
    }

    @POST
    @Path("/delete/{id}")
    @Transactional
    public ServiceDataResult<MatchDto> deleteMatchAlt(@PathParam("id") long id) {
        return new MatchService(entityManager).deleteMatch(id);
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
