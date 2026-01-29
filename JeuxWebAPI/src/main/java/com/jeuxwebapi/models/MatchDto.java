package com.jeuxwebapi.models;

import java.time.OffsetDateTime;

public class MatchDto {
    private long id;
    private int tour;
    private String round;
    private OffsetDateTime date;
    private int hScore;
    private int gScore;
    private String city;
    private String stadium;
    private Long leagueId;
    private Long tournamentId;
    private Long stageId;
    private Long hTeamId;
    private Long gTeamId;

    public MatchDto() {
    }

    public MatchDto(
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
        this.id = id;
        this.tour = tour;
        this.round = round;
        this.date = date;
        this.hScore = hScore;
        this.gScore = gScore;
        this.city = city;
        this.stadium = stadium;
        this.leagueId = leagueId;
        this.tournamentId = tournamentId;
        this.stageId = stageId;
        this.hTeamId = hTeamId;
        this.gTeamId = gTeamId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getTour() {
        return tour;
    }

    public void setTour(int tour) {
        this.tour = tour;
    }

    public String getRound() {
        return round;
    }

    public void setRound(String round) {
        this.round = round;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }

    public int getHScore() {
        return hScore;
    }

    public void setHScore(int hScore) {
        this.hScore = hScore;
    }

    public int getGScore() {
        return gScore;
    }

    public void setGScore(int gScore) {
        this.gScore = gScore;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStadium() {
        return stadium;
    }

    public void setStadium(String stadium) {
        this.stadium = stadium;
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

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public Long getHTeamId() {
        return hTeamId;
    }

    public void setHTeamId(Long hTeamId) {
        this.hTeamId = hTeamId;
    }

    public Long getGTeamId() {
        return gTeamId;
    }

    public void setGTeamId(Long gTeamId) {
        this.gTeamId = gTeamId;
    }
}
