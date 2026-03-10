package Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "\"Stage\"",
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

    @Column(name = "league_id", nullable = false)
    // Stage league id
    private Long LeagueId;

    @Column(name = "tournament_id", nullable = false)
    // Stage tournament id
    private Long TournamentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage_type", length = 16, nullable = false)
    // Stage type
    private StageType StageType = Entities.StageType.REGULAR;

    @ManyToOne
    @JoinColumn(
            name = "league_id",
            nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_stage_league_id"),
            insertable = false,
            updatable = false
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    // Stage league
    private League League;

    @ManyToOne
    @JoinColumn(
            name = "tournament_id",
            nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_stage_tournament_id"),
            insertable = false,
            updatable = false
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
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

    public Long getLeagueId() {
        return LeagueId;
    }

    public void setLeagueId(Long leagueId) {
        this.LeagueId = leagueId;
        this.League = null;
    }

    public Long getTournamentId() {
        return TournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.TournamentId = tournamentId;
        this.Tournament = null;
    }

    public League getLeague() {
        return League;
    }

    public void setLeague(League league) {
        this.League = league;
        this.LeagueId = league == null ? null : league.getId();
    }

    public Tournament getTournament() {
        return Tournament;
    }

    public void setTournament(Tournament tournament) {
        this.Tournament = tournament;
        this.TournamentId = tournament == null ? null : tournament.getId();
    }

    public StageType getStageType() {
        return StageType;
    }

    public void setStageType(StageType stageType) {
        this.StageType = stageType;
    }

    public List<Match> getMatches() {
        return Matches;
    }

    public void setMatches(List<Match> matches) {
        this.Matches = matches;
    }
}
