package Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "Match",
        indexes = {
                @jakarta.persistence.Index(name = "idx_match_league_id", columnList = "league_id"),
                @jakarta.persistence.Index(name = "idx_match_tournament_id", columnList = "tournament_id"),
                @jakarta.persistence.Index(name = "idx_match_stage_id", columnList = "stage_id"),
                @jakarta.persistence.Index(name = "idx_match_h_team_id", columnList = "h_team_id"),
                @jakarta.persistence.Index(name = "idx_match_g_team_id", columnList = "g_team_id"),
                @jakarta.persistence.Index(name = "idx_match_tour", columnList = "tour"),
                @jakarta.persistence.Index(name = "idx_match_round", columnList = "round"),
                @jakarta.persistence.Index(name = "idx_match_date", columnList = "date"),
                @jakarta.persistence.Index(name = "idx_match_h_score", columnList = "h_score"),
                @jakarta.persistence.Index(name = "idx_match_g_score", columnList = "g_score"),
               
        }
)
// Match table entity
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    // Match primary key
    private long Id;

    @Column(name = "tour", nullable = false)
    // Match tour number
    private int Tour;

    @Column(name = "round", length = 16, nullable = false)
    // Match round label
    private String Round;

    @Column(name = "date", nullable = true)
    // Match date/time (nullable)
    private OffsetDateTime Date;

    @Column(name = "h_score", nullable = false)
    // Home team score
    private int HScore;

    @Column(name = "g_score", nullable = false)
    // Guest team score
    private int GScore;

    @Column(name = "city", length = 128, nullable = true)
    // Match city (nullable)
    private String City;

    @Column(name = "stadium", length = 128, nullable = true)
    // Match stadium (nullable)
    private String Stadium;

    @ManyToOne
    @JoinColumn(
            name = "league_id",
            nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_match_league_id")
    )
    // Match league
    private League League;

    @ManyToOne
    @JoinColumn(
            name = "tournament_id",
            nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_match_tournament_id")
    )
    // Match tournament
    private Tournament Tournament;

    @ManyToOne
    @JoinColumn(
            name = "stage_id",
            nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_match_stage_id")
    )
    // Match stage
    private Stage Stage;

    @OneToOne
    @JoinColumn(
            name = "h_team_id",
            nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_match_h_team_id")
    )
    // Home team
    private Team HTeam;

    @OneToOne
    @JoinColumn(
            name = "g_team_id",
            nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_match_g_team_id")
    )
    // Guest team
    private Team GTeam;

    public Match() {
    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        this.Id = id;
    }

    public int getTour() {
        return Tour;
    }

    public void setTour(int tour) {
        this.Tour = tour;
    }

    public String getRound() {
        return Round;
    }

    public void setRound(String round) {
        this.Round = round;
    }

    public OffsetDateTime getDate() {
        return Date;
    }

    public void setDate(OffsetDateTime date) {
        this.Date = date;
    }

    public int getHScore() {
        return HScore;
    }

    public void setHScore(int hScore) {
        this.HScore = hScore;
    }

    public int getGScore() {
        return GScore;
    }

    public void setGScore(int gScore) {
        this.GScore = gScore;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        this.City = city;
    }

    public String getStadium() {
        return Stadium;
    }

    public void setStadium(String stadium) {
        this.Stadium = stadium;
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

    public Stage getStage() {
        return Stage;
    }

    public void setStage(Stage stage) {
        this.Stage = stage;
    }

    public Team getHTeam() {
        return HTeam;
    }

    public void setHTeam(Team hTeam) {
        this.HTeam = hTeam;
    }

    public Team getGTeam() {
        return GTeam;
    }

    public void setGTeam(Team gTeam) {
        this.GTeam = gTeam;
    }
}
