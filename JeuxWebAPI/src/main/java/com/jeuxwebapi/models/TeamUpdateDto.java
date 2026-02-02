package com.jeuxwebapi.models;

public class TeamUpdateDto extends TeamCreateDto {
    private long id;

    public TeamUpdateDto() {
    }

    public TeamUpdateDto(long id, String name, String shortName, String city, String logoUrl) {
        super(name, shortName, city, logoUrl);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
