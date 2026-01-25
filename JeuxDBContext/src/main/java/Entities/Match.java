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

    @Column(name = "league_id", nullable = false)
    // Match league id
    private Long LeagueId;

    @Column(name = "tournament_id", nullable = false)
    // Match tournament id
    private Long TournamentId;

    @Column(name = "stage_id", nullable = false)
    // Match stage id
    private Long StageId;

    @Column(name = "h_team_id", nullable = false)
    // Match home team id
    private Long HTeamId;

    @Column(name = "g_team_id", nullable = false)
    // Match guest team id
    private Long GTeamId;

    @ManyToOne
    @JoinColumn(
            name = "league_id",
            nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_match_league_id"),
            insertable = false,
            updatable = false
    )
    // Match league
    private League League;

    @ManyToOne
    @JoinColumn(
            name = "tournament_id",
            nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_match_tournament_id"),
            insertable = false,
            updatable = false
    )
    // Match tournament
    private Tournament Tournament;

    @ManyToOne
    @JoinColumn(
            name = "stage_id",
            nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_match_stage_id"),
            insertable = false,
            updatable = false
    )
    // Match stage
    private Stage Stage;

    @OneToOne
    @JoinColumn(
            name = "h_team_id",
            nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_match_h_team_id"),
            insertable = false,
            updatable = false
    )
    // Home team
    private Team HTeam;

    @OneToOne
    @JoinColumn(
            name = "g_team_id",
            nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_match_g_team_id"),
            insertable = false,
            updatable = false
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

    public Long getStageId() {
        return StageId;
    }

    public void setStageId(Long stageId) {
        this.StageId = stageId;
        this.Stage = null;
    }

    public Long getHTeamId() {
        return HTeamId;
    }

    public void setHTeamId(Long hTeamId) {
        this.HTeamId = hTeamId;
        this.HTeam = null;
    }

    public Long getGTeamId() {
        return GTeamId;
    }

    public void setGTeamId(Long gTeamId) {
        this.GTeamId = gTeamId;
        this.GTeam = null;
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

    public Stage getStage() {
        return Stage;
    }

    public void setStage(Stage stage) {
        this.Stage = stage;
        this.StageId = stage == null ? null : stage.getId();
    }

    public Team getHTeam() {
        return HTeam;
    }

    public void setHTeam(Team hTeam) {
        this.HTeam = hTeam;
        this.HTeamId = hTeam == null ? null : hTeam.getId();
    }

    public Team getGTeam() {
        return GTeam;
    }

    public void setGTeam(Team gTeam) {
        this.GTeam = gTeam;
        this.GTeamId = gTeam == null ? null : gTeam.getId();
    }
}
