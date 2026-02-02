package com.jeuxwebapi.models;

public class TournamentUpdateDto extends TournamentCreateDto {
    private long id;

    public TournamentUpdateDto() {
    }

    public TournamentUpdateDto(long id, String name, int stYear, Integer fnYear, Long leagueId) {
        super(name, stYear, fnYear, leagueId);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
