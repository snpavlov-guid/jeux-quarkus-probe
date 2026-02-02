package com.jeuxwebapi.models;

public class TeamCreateDto {
    private String name;
    private String shortName;
    private String city;
    private String logoUrl;

    public TeamCreateDto() {
    }

    public TeamCreateDto(String name, String shortName, String city, String logoUrl) {
        this.name = name;
        this.shortName = shortName;
        this.city = city;
        this.logoUrl = logoUrl;
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
