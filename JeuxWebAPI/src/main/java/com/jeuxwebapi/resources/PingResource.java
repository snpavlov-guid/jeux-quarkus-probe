package com.jeuxwebapi.resources;

import com.jeuxwebapi.models.PingResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.time.OffsetDateTime;

@Path("/ping")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PingResource {
    @GET
    public PingResponse ping(@QueryParam("echo") String echo) {
        return new PingResponse(echo, OffsetDateTime.now());
    }
}
