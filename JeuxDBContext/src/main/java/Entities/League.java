package Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "\"League\"",
        indexes = {
                @jakarta.persistence.Index(name = "idx_league_name", columnList = "name")
        }
)
// League table entity
public class League {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    // League primary key
    private long Id;

    @Column(name = "name", length = 128, nullable = false)
    // League display name
    private String Name;

    @OneToMany(mappedBy = "League")
    // League tournaments
    private List<Tournament> Tournaments = new ArrayList<>();

    @OneToMany(mappedBy = "League")
    // League stages
    private List<Stage> Stages = new ArrayList<>();

    @OneToMany(mappedBy = "League")
    // League matches
    private List<Match> Matches = new ArrayList<>();

    public League() {
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

    public List<Tournament> getTournaments() {
        return Tournaments;
    }

    public void setTournaments(List<Tournament> tournaments) {
        this.Tournaments = tournaments;
    }

    public List<Stage> getStages() {
        return Stages;
    }

    public void setStages(List<Stage> stages) {
        this.Stages = stages;
    }

    public List<Match> getMatches() {
        return Matches;
    }

    public void setMatches(List<Match> matches) {
        this.Matches = matches;
    }
}
