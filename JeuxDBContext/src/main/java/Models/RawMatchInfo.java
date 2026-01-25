package Models;

public class RawMatchInfo {
    private String Stage;
    private String MatchId;
    private int Tour;
    private String Date;
    private String HTeam;
    private String GTeam;
    private int HTeamScore;
    private int GTeamScore;
    private String HTeamLogo;
    private String GTeamLogo;

    public RawMatchInfo() {
    }

    public String getStage() {
        return Stage;
    }

    public void setStage(String stage) {
        this.Stage = stage;
    }

    public String getMatchId() {
        return MatchId;
    }

    public void setMatchId(String matchId) {
        this.MatchId = matchId;
    }

    public int getTour() {
        return Tour;
    }

    public void setTour(int tour) {
        this.Tour = tour;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        this.Date = date;
    }

    public String getHTeam() {
        return HTeam;
    }

    public void setHTeam(String hTeam) {
        this.HTeam = hTeam;
    }

    public String getGTeam() {
        return GTeam;
    }

    public void setGTeam(String gTeam) {
        this.GTeam = gTeam;
    }

    public int getHTeamScore() {
        return HTeamScore;
    }

    public void setHTeamScore(int hTeamScore) {
        this.HTeamScore = hTeamScore;
    }

    public int getGTeamScore() {
        return GTeamScore;
    }

    public void setGTeamScore(int gTeamScore) {
        this.GTeamScore = gTeamScore;
    }

    public String getHTeamLogo() {
        return HTeamLogo;
    }

    public void setHTeamLogo(String hTeamLogo) {
        this.HTeamLogo = hTeamLogo;
    }

    public String getGTeamLogo() {
        return GTeamLogo;
    }

    public void setGTeamLogo(String gTeamLogo) {
        this.GTeamLogo = gTeamLogo;
    }
}
