package com.jeuxwebapi.models;

public class LeagueUpdateDto extends LeagueCreateDto {
    private long id;

    public LeagueUpdateDto() {
    }

    public LeagueUpdateDto(long id, String name) {
        super(name);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
