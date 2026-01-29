package com.jeuxwebapi.models;

public class TeamDto {
    private long id;
    private String name;
    private String shortName;
    private String city;
    private String logoUrl;

    public TeamDto() {
    }

    public TeamDto(long id, String name, String shortName, String city, String logoUrl) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.city = city;
        this.logoUrl = logoUrl;
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

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
}
