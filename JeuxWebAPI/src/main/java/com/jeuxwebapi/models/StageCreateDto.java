package com.jeuxwebapi.models;

import Enums.PrevPlaysType;
import Enums.StageType;
import java.util.List;

public class StageCreateDto {
    private String name;
    private int order;
    private Long leagueId;
    private Long tournamentId;
    private StageType stageType;
    private List<String> groups;
    private Long prevStageId;
    private PrevPlaysType prevPlays;

    public StageCreateDto() {
    }

    public StageCreateDto(
            String name,
            int order,
            Long leagueId,
            Long tournamentId,
            StageType stageType,
            List<String> groups,
            Long prevStageId,
            PrevPlaysType prevPlays
    ) {
        this.name = name;
        this.order = order;
        this.leagueId = leagueId;
        this.tournamentId = tournamentId;
        this.stageType = stageType;
        this.groups = groups;
        this.prevStageId = prevStageId;
        this.prevPlays = prevPlays;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Long getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(Long leagueId) {
        this.leagueId = leagueId;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public StageType getStageType() {
        return stageType;
    }

    public void setStageType(StageType stageType) {
        this.stageType = stageType;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public Long getPrevStageId() {
        return prevStageId;
    }

    public void setPrevStageId(Long prevStageId) {
        this.prevStageId = prevStageId;
    }

    public PrevPlaysType getPrevPlays() {
        return prevPlays;
    }

    public void setPrevPlays(PrevPlaysType prevPlays) {
        this.prevPlays = prevPlays;
    }
}
