package com.jeuxwebapi.models;

public class TournamentCreateDto {
    private String name;
    private int stYear;
    private Integer fnYear;
    private Long leagueId;

    public TournamentCreateDto() {
    }

    public TournamentCreateDto(String name, int stYear, Integer fnYear, Long leagueId) {
        this.name = name;
        this.stYear = stYear;
        this.fnYear = fnYear;
        this.leagueId = leagueId;
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
}
