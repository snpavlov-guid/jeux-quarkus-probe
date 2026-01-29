package com.jeuxwebapi.models;

public class TournamentDto {
    private long id;
    private String name;
    private int stYear;
    private Integer fnYear;
    private Long leagueId;
    private String seasonLabel;

    public TournamentDto() {
    }

    public TournamentDto(long id, String name, int stYear, Integer fnYear, Long leagueId, String seasonLabel) {
        this.id = id;
        this.name = name;
        this.stYear = stYear;
        this.fnYear = fnYear;
        this.leagueId = leagueId;
        this.seasonLabel = seasonLabel;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getSeasonLabel() {
        return seasonLabel;
    }

    public void setSeasonLabel(String seasonLabel) {
        this.seasonLabel = seasonLabel;
    }
}
