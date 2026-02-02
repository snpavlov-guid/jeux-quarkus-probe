package com.jeuxwebapi.models;

import java.time.OffsetDateTime;

public class MatchUpdateDto extends MatchCreateDto {
    private long id;

    public MatchUpdateDto() {
    }

    public MatchUpdateDto(
            long id,
            int tour,
            String round,
            OffsetDateTime date,
            int hScore,
            int gScore,
            String city,
            String stadium,
            Long leagueId,
            Long tournamentId,
            Long stageId,
            Long hTeamId,
            Long gTeamId
    ) {
        super(tour, round, date, hScore, gScore, city, stadium, leagueId, tournamentId, stageId, hTeamId, gTeamId);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
