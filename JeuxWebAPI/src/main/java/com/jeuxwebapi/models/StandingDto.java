package com.jeuxwebapi.models;

public class StandingDto {
    private long teamId;
    private String teamName;
    private String teamLogo;
    private int matches;
    private int wins;
    private int draw;
    private int lost;
    private int points;
    private int scored;
    private int missed;
    private int diff;
    private int hMatches;
    private int hWins;
    private int hDraw;
    private int hLost;
    private int hPoints;
    private int hScored;
    private int hMissed;
    private int hDiff;
    private int gMatches;
    private int gWins;
    private int gDraw;
    private int gLost;
    private int gPoints;
    private int gScored;
    private int gMissed;
    private int gDiff;
    private int order;
    private int subOrder;

    public StandingDto() {
    }

    public StandingDto(
            long teamId,
            String teamName,
            String teamLogo,
            int matches,
            int wins,
            int draw,
            int lost,
            int points,
            int scored,
            int missed,
            int diff,
            int hMatches,
            int hWins,
            int hDraw,
            int hLost,
            int hPoints,
            int hScored,
            int hMissed,
            int hDiff,
            int gMatches,
            int gWins,
            int gDraw,
            int gLost,
            int gPoints,
            int gScored,
            int gMissed,
            int gDiff
    ) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.teamLogo = teamLogo;
        this.matches = matches;
        this.wins = wins;
        this.draw = draw;
        this.lost = lost;
        this.points = points;
        this.scored = scored;
        this.missed = missed;
        this.diff = diff;
        this.hMatches = hMatches;
        this.hWins = hWins;
        this.hDraw = hDraw;
        this.hLost = hLost;
        this.hPoints = hPoints;
        this.hScored = hScored;
        this.hMissed = hMissed;
        this.hDiff = hDiff;
        this.gMatches = gMatches;
        this.gWins = gWins;
        this.gDraw = gDraw;
        this.gLost = gLost;
        this.gPoints = gPoints;
        this.gScored = gScored;
        this.gMissed = gMissed;
        this.gDiff = gDiff;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamLogo() {
        return teamLogo;
    }

    public void setTeamLogo(String teamLogo) {
        this.teamLogo = teamLogo;
    }

    public int getMatches() {
        return matches;
    }

    public void setMatches(int matches) {
        this.matches = matches;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }

    public int getLost() {
        return lost;
    }

    public void setLost(int lost) {
        this.lost = lost;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getScored() {
        return scored;
    }

    public void setScored(int scored) {
        this.scored = scored;
    }

    public int getMissed() {
        return missed;
    }

    public void setMissed(int missed) {
        this.missed = missed;
    }

    public int getDiff() {
        return diff;
    }

    public void setDiff(int diff) {
        this.diff = diff;
    }

    public int getHMatches() {
        return hMatches;
    }

    public void setHMatches(int hMatches) {
        this.hMatches = hMatches;
    }

    public int getHWins() {
        return hWins;
    }

    public void setHWins(int hWins) {
        this.hWins = hWins;
    }

    public int getHDraw() {
        return hDraw;
    }

    public void setHDraw(int hDraw) {
        this.hDraw = hDraw;
    }

    public int getHLost() {
        return hLost;
    }

    public void setHLost(int hLost) {
        this.hLost = hLost;
    }

    public int getHPoints() {
        return hPoints;
    }

    public void setHPoints(int hPoints) {
        this.hPoints = hPoints;
    }

    public int getHScored() {
        return hScored;
    }

    public void setHScored(int hScored) {
        this.hScored = hScored;
    }

    public int getHMissed() {
        return hMissed;
    }

    public void setHMissed(int hMissed) {
        this.hMissed = hMissed;
    }

    public int getHDiff() {
        return hDiff;
    }

    public void setHDiff(int hDiff) {
        this.hDiff = hDiff;
    }

    public int getGMatches() {
        return gMatches;
    }

    public void setGMatches(int gMatches) {
        this.gMatches = gMatches;
    }

    public int getGWins() {
        return gWins;
    }

    public void setGWins(int gWins) {
        this.gWins = gWins;
    }

    public int getGDraw() {
        return gDraw;
    }

    public void setGDraw(int gDraw) {
        this.gDraw = gDraw;
    }

    public int getGLost() {
        return gLost;
    }

    public void setGLost(int gLost) {
        this.gLost = gLost;
    }

    public int getGPoints() {
        return gPoints;
    }

    public void setGPoints(int gPoints) {
        this.gPoints = gPoints;
    }

    public int getGScored() {
        return gScored;
    }

    public void setGScored(int gScored) {
        this.gScored = gScored;
    }

    public int getGMissed() {
        return gMissed;
    }

    public void setGMissed(int gMissed) {
        this.gMissed = gMissed;
    }

    public int getGDiff() {
        return gDiff;
    }

    public void setGDiff(int gDiff) {
        this.gDiff = gDiff;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getSubOrder() {
        return subOrder;
    }

    public void setSubOrder(int subOrder) {
        this.subOrder = subOrder;
    }
}
