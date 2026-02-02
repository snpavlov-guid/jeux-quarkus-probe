package com.jeuxwebapitest;

import com.jeuxwebapi.resources.LeagueResource;
import com.jeuxwebapi.resources.MatchResource;
import com.jeuxwebapi.resources.PingResource;
import com.jeuxwebapi.resources.TeamResource;
import com.jeuxwebapi.resources.TournamentResource;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.Set;

@ApplicationPath("/api/q/v1")
public class TestApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
                LeagueResource.class,
                MatchResource.class,
                PingResource.class,
                TeamResource.class,
                TournamentResource.class
        );
    }
}
