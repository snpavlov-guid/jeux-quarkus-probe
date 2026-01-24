package Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(
        name = "Team",
        indexes = {
            @jakarta.persistence.Index(name = "idx_team_name", columnList = "name"),
            @jakarta.persistence.Index(name = "idx_team_short_name", columnList = "short_name"),
            @jakarta.persistence.Index(name = "idx_team_city", columnList = "city"),

        }
)
// Team table entity
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    // Team primary key
    private long Id;

    @Column(name = "name", length = 128, nullable = false)
    // Team display name
    private String Name;

    @Column(name = "short_name", length = 6, nullable = false)
    // Team short name
    private String ShortName;

    @Column(name = "city", length = 128, nullable = true)
    // Team city
    private String City;

    @Column(name = "logo_url", length = 256, nullable = true)
    // Team logo URL (nullable)
    private String LogoUrl;

    public Team() {
    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        this.Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getShortName() {
        return ShortName;
    }

    public void setShortName(String shortName) {
        this.ShortName = shortName;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        this.City = city;
    }

    public String getLogoUrl() {
        return LogoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.LogoUrl = logoUrl;
    }
}
