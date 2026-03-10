package com.jeuxwebapitest.util;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.h2.tools.SimpleResultSet;

public final class H2SqlFunctions {
    private H2SqlFunctions() {
    }

    public static Date date(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return new Date(timestamp.getTime());
    }

    public static ResultSet getTournamentStandings(Connection connection, Long leagueId, Long tournamentId, Long stageId)
            throws SQLException {
        SimpleResultSet resultSet = createStandingResultSet();
        if (leagueId == null || tournamentId == null || stageId == null) {
            return resultSet;
        }

        Map<Long, TeamStats> teams = new HashMap<>();

        try (PreparedStatement statement = connection.prepareStatement(
                """
                select h_team_id, g_team_id, h_score, g_score
                from football."Match"
                where league_id = ? and tournament_id = ? and stage_id = ?
                """
        )) {
            statement.setLong(1, leagueId);
            statement.setLong(2, tournamentId);
            statement.setLong(3, stageId);

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    long homeTeamId = rows.getLong(1);
                    long guestTeamId = rows.getLong(2);
                    int homeScore = rows.getInt(3);
                    int guestScore = rows.getInt(4);

                    TeamStats homeStats = teams.computeIfAbsent(homeTeamId, TeamStats::new);
                    TeamStats guestStats = teams.computeIfAbsent(guestTeamId, TeamStats::new);

                    homeStats.applyHomeMatch(homeScore, guestScore);
                    guestStats.applyGuestMatch(guestScore, homeScore);
                }
            }
        }

        loadTeamNames(connection, teams);

        List<TeamStats> ordered = new ArrayList<>(teams.values());
        ordered.sort(Comparator
                .comparingInt(TeamStats::getPoints).reversed()
                .thenComparingInt(TeamStats::getWins).reversed()
                .thenComparingInt(TeamStats::getDiff).reversed()
                .thenComparingInt(TeamStats::getScored).reversed()
                .thenComparingInt(TeamStats::getGWins).reversed()
                .thenComparingInt(TeamStats::getGScored).reversed()
                .thenComparingLong(TeamStats::getTeamId));

        for (TeamStats stats : ordered) {
            resultSet.addRow(
                    stats.teamId,
                    stats.teamName,
                    stats.teamLogo,
                    stats.getMatches(),
                    stats.getWins(),
                    stats.getDraw(),
                    stats.getLost(),
                    stats.getPoints(),
                    stats.getScored(),
                    stats.getMissed(),
                    stats.getDiff(),
                    stats.hMatches,
                    stats.hWins,
                    stats.hDraw,
                    stats.hLost,
                    stats.hPoints,
                    stats.hScored,
                    stats.hMissed,
                    stats.hDiff,
                    stats.gMatches,
                    stats.gWins,
                    stats.gDraw,
                    stats.gLost,
                    stats.gPoints,
                    stats.gScored,
                    stats.gMissed,
                    stats.gDiff
            );
        }

        return resultSet;
    }

    private static SimpleResultSet createStandingResultSet() {
        SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn("teamid", Types.BIGINT, 20, 0);
        resultSet.addColumn("teamname", Types.VARCHAR, 128, 0);
        resultSet.addColumn("teamlogo", Types.VARCHAR, 256, 0);
        resultSet.addColumn("matches", Types.INTEGER, 10, 0);
        resultSet.addColumn("wins", Types.INTEGER, 10, 0);
        resultSet.addColumn("draw", Types.INTEGER, 10, 0);
        resultSet.addColumn("lost", Types.INTEGER, 10, 0);
        resultSet.addColumn("points", Types.INTEGER, 10, 0);
        resultSet.addColumn("scored", Types.INTEGER, 10, 0);
        resultSet.addColumn("missed", Types.INTEGER, 10, 0);
        resultSet.addColumn("diff", Types.INTEGER, 10, 0);
        resultSet.addColumn("h_matches", Types.INTEGER, 10, 0);
        resultSet.addColumn("h_wins", Types.INTEGER, 10, 0);
        resultSet.addColumn("h_draw", Types.INTEGER, 10, 0);
        resultSet.addColumn("h_lost", Types.INTEGER, 10, 0);
        resultSet.addColumn("h_points", Types.INTEGER, 10, 0);
        resultSet.addColumn("h_scored", Types.INTEGER, 10, 0);
        resultSet.addColumn("h_missed", Types.INTEGER, 10, 0);
        resultSet.addColumn("h_diff", Types.INTEGER, 10, 0);
        resultSet.addColumn("g_matches", Types.INTEGER, 10, 0);
        resultSet.addColumn("g_wins", Types.INTEGER, 10, 0);
        resultSet.addColumn("g_draw", Types.INTEGER, 10, 0);
        resultSet.addColumn("g_lost", Types.INTEGER, 10, 0);
        resultSet.addColumn("g_points", Types.INTEGER, 10, 0);
        resultSet.addColumn("g_scored", Types.INTEGER, 10, 0);
        resultSet.addColumn("g_missed", Types.INTEGER, 10, 0);
        resultSet.addColumn("g_diff", Types.INTEGER, 10, 0);
        return resultSet;
    }

    private static void loadTeamNames(Connection connection, Map<Long, TeamStats> teams) throws SQLException {
        if (teams.isEmpty()) {
            return;
        }
        try (PreparedStatement statement = connection.prepareStatement("select id, name, logo_url from football.\"Team\"")) {
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    long id = rows.getLong(1);
                    TeamStats stats = teams.get(id);
                    if (stats != null) {
                        stats.teamName = rows.getString(2);
                        stats.teamLogo = rows.getString(3);
                    }
                }
            }
        }
    }

    private static final class TeamStats {
        private final long teamId;
        private String teamName;
        private String teamLogo;
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

        private TeamStats(long teamId) {
            this.teamId = teamId;
        }

        private void applyHomeMatch(int scored, int missed) {
            hMatches++;
            hScored += scored;
            hMissed += missed;
            hDiff += (scored - missed);
            if (scored > missed) {
                hWins++;
                hPoints += 3;
            } else if (scored == missed) {
                hDraw++;
                hPoints += 1;
            } else {
                hLost++;
            }
        }

        private void applyGuestMatch(int scored, int missed) {
            gMatches++;
            gScored += scored;
            gMissed += missed;
            gDiff += (scored - missed);
            if (scored > missed) {
                gWins++;
                gPoints += 3;
            } else if (scored == missed) {
                gDraw++;
                gPoints += 1;
            } else {
                gLost++;
            }
        }

        private long getTeamId() {
            return teamId;
        }

        private int getMatches() {
            return hMatches + gMatches;
        }

        private int getWins() {
            return hWins + gWins;
        }

        private int getDraw() {
            return hDraw + gDraw;
        }

        private int getLost() {
            return hLost + gLost;
        }

        private int getPoints() {
            return hPoints + gPoints;
        }

        private int getScored() {
            return hScored + gScored;
        }

        private int getMissed() {
            return hMissed + gMissed;
        }

        private int getDiff() {
            return hDiff + gDiff;
        }

        private int getGWins() {
            return gWins;
        }

        private int getGScored() {
            return gScored;
        }
    }
}
