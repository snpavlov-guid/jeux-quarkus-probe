package com.jeuxwebapi.models;

import Enums.PrevPlaysType;
import Enums.StageType;
import java.util.List;

public class StageUpdateDto extends StageCreateDto {
    private long id;

    public StageUpdateDto() {
    }

    public StageUpdateDto(
            long id,
            String name,
            int order,
            Long leagueId,
            Long tournamentId,
            StageType stageType,
            List<String> groups,
            Long prevStageId,
            PrevPlaysType prevPlays
    ) {
        super(name, order, leagueId, tournamentId, stageType, groups, prevStageId, prevPlays);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
