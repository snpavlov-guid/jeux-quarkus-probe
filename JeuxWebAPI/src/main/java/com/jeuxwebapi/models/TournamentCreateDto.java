package com.jeuxwebapi.models;

import java.util.List;

public class TournamentCreateDto {
    private String name;
    private int stYear;
    private Integer fnYear;
    private Long leagueId;
    private List<StageUpdateDto> stages;

    public TournamentCreateDto() {
    }

    public TournamentCreateDto(String name, int stYear, Integer fnYear, Long leagueId, List<StageUpdateDto> stages) {
        this.name = name;
        this.stYear = stYear;
        this.fnYear = fnYear;
        this.leagueId = leagueId;
        this.stages = stages;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStYear() {
        return stYear;
    }

    public void setStYear(int stYear) {
        this.stYear = stYear;
    }

    public Integer getFnYear() {
        return fnYear;
    }

    public void setFnYear(Integer fnYear) {
        this.fnYear = fnYear;
    }

    public Long getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(Long leagueId) {
        this.leagueId = leagueId;
    }

    public List<StageUpdateDto> getStages() {
        return stages;
    }

    public void setStages(List<StageUpdateDto> stages) {
        this.stages = stages;
    }
}
