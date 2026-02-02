package com.jeuxwebapi.models;

public class LeagueCreateDto {
    private String name;

    public LeagueCreateDto() {
    }

    public LeagueCreateDto(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
