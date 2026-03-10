package com.jeuxwebapi.models;

public class StageCreateDto {
    private String name;
    private int order;
    private Long leagueId;
    private Long tournamentId;
    private StageType stageType;

    public StageCreateDto() {
    }

    public StageCreateDto(String name, int order, Long leagueId, Long tournamentId, StageType stageType) {
        this.name = name;
        this.order = order;
        this.leagueId = leagueId;
        this.tournamentId = tournamentId;
        this.stageType = stageType;
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
}
