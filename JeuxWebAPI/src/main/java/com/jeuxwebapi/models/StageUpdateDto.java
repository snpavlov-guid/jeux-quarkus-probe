package com.jeuxwebapi.models;

public class StageUpdateDto extends StageCreateDto {
    private long id;

    public StageUpdateDto() {
    }

    public StageUpdateDto(long id, String name, int order, Long leagueId, Long tournamentId) {
        super(name, order, leagueId, tournamentId);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
