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
        name = "Stage",
        indexes = {
                @jakarta.persistence.Index(name = "idx_stage_league_id", columnList = "league_id"),
                @jakarta.persistence.Index(name = "idx_stage_tournament_id", columnList = "tournament_id"),
                @jakarta.persistence.Index(name = "idx_stage_order", columnList = "order"),
                @jakarta.persistence.Index(name = "idx_stage_name", columnList = "name")
  
        }
)
// Stage table entity
public class Stage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    // Stage primary key
    private long Id;

    @Column(name = "name", length = 128, nullable = false)
    // Stage display name
    private String Name;

    @Column(name = "order", nullable = false)
    // Stage ordering number
    private int Order;

    @ManyToOne
    @JoinColumn(
            name = "league_id",
            nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_stage_league_id")
    )
    // Stage league
    private League League;

    @ManyToOne
    @JoinColumn(
            name = "tournament_id",
            nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_stage_tournament_id")
    )
    // Stage tournament
    private Tournament Tournament;

    @OneToMany(mappedBy = "Stage")
    // Stage matches
    private List<Match> Matches = new ArrayList<>();

    public Stage() {
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

    public int getOrder() {
        return Order;
    }

    public void setOrder(int order) {
        this.Order = order;
    }

    public League getLeague() {
        return League;
    }

    public void setLeague(League league) {
        this.League = league;
    }

    public Tournament getTournament() {
        return Tournament;
    }

    public void setTournament(Tournament tournament) {
        this.Tournament = tournament;
    }

    public List<Match> getMatches() {
        return Matches;
    }

    public void setMatches(List<Match> matches) {
        this.Matches = matches;
    }
}
