package Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "Tournament",
        indexes = {
                @jakarta.persistence.Index(name = "idx_tournament_league_id", columnList = "league_id"),
                @jakarta.persistence.Index(name = "idx_tournament_st_year", columnList = "st_year"),
                @jakarta.persistence.Index(name = "idx_tournament_fn_year", columnList = "fn_year"),
                @jakarta.persistence.Index(name = "idx_tournament_name", columnList = "name")
        }
)
// Tournament table entity
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    // Tournament primary key
    private long Id;

    @Column(name = "name", length = 128, nullable = false)
    // Tournament display name
    private String Name;

    @Column(name = "st_year", nullable = false)
    // Tournament start year
    private int StYear;

    @Column(name = "fn_year", nullable = true)
    // Tournament finish year (nullable)
    private Integer FnYear;

    @ManyToOne
    @JoinColumn(
            name = "league_id",
            nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_tournament_league_id")
    )
    // Tournament league
    private League League;

    @OneToMany(mappedBy = "Tournament")
    // Tournament stages
    private List<Stage> Stages = new ArrayList<>();

    @OneToMany(mappedBy = "Tournament")
    // Tournament matches
    private List<Match> Matches = new ArrayList<>();

    public Tournament() {
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

    public int getStYear() {
        return StYear;
    }

    public void setStYear(int stYear) {
        this.StYear = stYear;
    }

    public Integer getFnYear() {
        return FnYear;
    }

    public void setFnYear(Integer fnYear) {
        this.FnYear = fnYear;
    }

    public String getSeasonLabel() {
        if (FnYear == null) {
            return String.valueOf(StYear);
        }
        return StYear + "-" + FnYear;
    }

    public League getLeague() {
        return League;
    }

    public void setLeague(League league) {
        this.League = league;
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
