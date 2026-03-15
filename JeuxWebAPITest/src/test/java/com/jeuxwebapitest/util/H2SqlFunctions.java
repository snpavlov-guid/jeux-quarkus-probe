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
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public static ResultSet getHomeMatchResults(
            Connection connection,
            Long leagueId,
            Long tournamentId,
            Long stageId,
            String group,
            Long[] teamIds
    ) throws SQLException {
        return getMatchResults(connection, leagueId, tournamentId, stageId, group, teamIds, true);
    }

    public static ResultSet getGuestMatchResults(
            Connection connection,
            Long leagueId,
            Long tournamentId,
            Long stageId,
            String group,
            Long[] teamIds
    ) throws SQLException {
        return getMatchResults(connection, leagueId, tournamentId, stageId, group, teamIds, false);
    }

    public static ResultSet getAllMatchResults(
            Connection connection,
            Long leagueId,
            Long tournamentId,
            Long stageId,
            String group,
            Long[] teamIds,
            Integer matchType
    ) throws SQLException {
        SimpleResultSet resultSet = createMatchResultsResultSet();
        if (matchType != null && matchType == 1) {
            appendRows(getMatchResults(connection, leagueId, tournamentId, stageId, group, teamIds, true), resultSet);
            return resultSet;
        }
        if (matchType != null && matchType == -1) {
            appendRows(getMatchResults(connection, leagueId, tournamentId, stageId, group, teamIds, false), resultSet);
            return resultSet;
        }

        appendRows(getMatchResults(connection, leagueId, tournamentId, stageId, group, teamIds, true), resultSet);
        appendRows(getMatchResults(connection, leagueId, tournamentId, stageId, group, teamIds, false), resultSet);
        return resultSet;
    }

    public static ResultSet getTournamentTeams(
            Connection connection,
            Long leagueId,
            Long tournamentId,
            Long stageId,
            String group
    ) throws SQLException {
        SimpleResultSet resultSet = createTournamentTeamsResultSet();
        if (leagueId == null || tournamentId == null || stageId == null) {
            return resultSet;
        }

        String normalizedGroup = group == null || group.isBlank() ? null : group;
        StringBuilder sql = new StringBuilder("""
                select distinct t.id, t.name, t.logo_url
                from football."Match" m
                join football."Team" t on t.id = m.h_team_id or t.id = m.g_team_id
                where m.league_id = ? and m.tournament_id = ? and m.stage_id = ?
                """);
        if (normalizedGroup != null) {
            sql.append(" and m.\"group\" = ?");
        }
        sql.append(" order by t.name");

        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            statement.setLong(index++, leagueId);
            statement.setLong(index++, tournamentId);
            statement.setLong(index++, stageId);
            if (normalizedGroup != null) {
                statement.setString(index, normalizedGroup);
            }

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    resultSet.addRow(
                            rows.getObject(1),
                            rows.getObject(2),
                            rows.getObject(3)
                    );
                }
            }
        }
        return resultSet;
    }

    public static ResultSet getTournamentStandingsEx(
            Connection connection,
            Long leagueId,
            Long tournamentId,
            Long stageId,
            String group,
            Long[] teamIds,
            Integer matchType,
            Long prevStageId,
            Integer prevPlays
    ) throws SQLException {
        SimpleResultSet resultSet = createStandingResultSet();
        if (leagueId == null || tournamentId == null || stageId == null) {
            return resultSet;
        }

        Map<Long, TeamStats> teams = new HashMap<>();
        try (ResultSet rows = getAllMatchResults(connection, leagueId, tournamentId, stageId, group, teamIds, matchType)) {
            while (rows.next()) {
                long teamId = ((Number) rows.getObject(1)).longValue();
                TeamStats stats = teams.computeIfAbsent(teamId, TeamStats::new);
                stats.applyResultRow(
                        toIntValue(rows.getObject(2)),
                        toIntValue(rows.getObject(3)),
                        toIntValue(rows.getObject(4)),
                        toIntValue(rows.getObject(5)),
                        toIntValue(rows.getObject(6)),
                        toIntValue(rows.getObject(7)),
                        toIntValue(rows.getObject(8)),
                        toIntValue(rows.getObject(9)),
                        toIntValue(rows.getObject(10)),
                        toIntValue(rows.getObject(11)),
                        toIntValue(rows.getObject(12)),
                        toIntValue(rows.getObject(13)),
                        toIntValue(rows.getObject(14)),
                        toIntValue(rows.getObject(15)),
                        toIntValue(rows.getObject(16)),
                        toIntValue(rows.getObject(17)),
                        toIntValue(rows.getObject(18)),
                        toIntValue(rows.getObject(19)),
                        toIntValue(rows.getObject(20)),
                        toIntValue(rows.getObject(21)),
                        toIntValue(rows.getObject(22))
                );
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

    private static ResultSet getMatchResults(
            Connection connection,
            Long leagueId,
            Long tournamentId,
            Long stageId,
            String group,
            Object teamIdsParam,
            boolean homeSide
    ) throws SQLException {
        SimpleResultSet resultSet = createMatchResultsResultSet();
        if (leagueId == null || tournamentId == null || stageId == null) {
            return resultSet;
        }

        String normalizedGroup = group == null || group.isBlank() ? null : group;
        Set<Long> teamIds = toTeamIdSet(teamIdsParam);

        StringBuilder sql = new StringBuilder("""
                select m.h_team_id, m.g_team_id, m.h_score, m.g_score, coalesce(t.win_pnt, 3)
                from football."Match" m
                join football."Tournament" t on t.id = m.tournament_id and t.league_id = m.league_id
                where m.league_id = ? and m.tournament_id = ? and m.stage_id = ?
                """);
        if (normalizedGroup != null) {
            sql.append(" and m.\"group\" = ?");
        }

        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            statement.setLong(index++, leagueId);
            statement.setLong(index++, tournamentId);
            statement.setLong(index++, stageId);
            if (normalizedGroup != null) {
                statement.setString(index, normalizedGroup);
            }

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    long homeTeamId = rows.getLong(1);
                    long guestTeamId = rows.getLong(2);
                    int homeScore = rows.getInt(3);
                    int guestScore = rows.getInt(4);
                    int winPoints = rows.getInt(5);

                    long teamId = homeSide ? homeTeamId : guestTeamId;
                    if (!teamIds.isEmpty() && !teamIds.contains(teamId)) {
                        continue;
                    }

                    int scored = homeSide ? homeScore : guestScore;
                    int missed = homeSide ? guestScore : homeScore;
                    int diff = scored - missed;
                    int wins = scored > missed ? 1 : 0;
                    int draw = scored == missed ? 1 : 0;
                    int lost = scored < missed ? 1 : 0;
                    int points = wins == 1 ? winPoints : draw == 1 ? 1 : 0;

                    if (homeSide) {
                        resultSet.addRow(
                                teamId,
                                wins,
                                draw,
                                lost,
                                points,
                                scored,
                                missed,
                                diff,
                                wins,
                                draw,
                                lost,
                                points,
                                scored,
                                missed,
                                diff,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0
                        );
                    } else {
                        resultSet.addRow(
                                teamId,
                                wins,
                                draw,
                                lost,
                                points,
                                scored,
                                missed,
                                diff,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                wins,
                                draw,
                                lost,
                                points,
                                scored,
                                missed,
                                diff
                        );
                    }
                }
            }
        }

        return resultSet;
    }

    private static Set<Long> toTeamIdSet(Object teamIdsParam) throws SQLException {
        if (teamIdsParam == null) {
            return Set.of();
        }

        if (teamIdsParam instanceof java.sql.Array sqlArray) {
            try {
                return toTeamIdSet(sqlArray.getArray());
            } finally {
                sqlArray.free();
            }
        }

        Set<Long> values = new HashSet<>();
        if (teamIdsParam instanceof Object[] arrayValues) {
            for (Object value : arrayValues) {
                if (value instanceof Number number) {
                    values.add(number.longValue());
                }
            }
            return values;
        }

        if (teamIdsParam instanceof Iterable<?> iterable) {
            for (Object value : iterable) {
                if (value instanceof Number number) {
                    values.add(number.longValue());
                }
            }
            return values;
        }

        if (teamIdsParam instanceof Number number) {
            values.add(number.longValue());
        }
        return values;
    }

    private static void appendRows(ResultSet source, SimpleResultSet target) throws SQLException {
        try (source) {
            while (source.next()) {
                target.addRow(
                        source.getObject(1),
                        source.getObject(2),
                        source.getObject(3),
                        source.getObject(4),
                        source.getObject(5),
                        source.getObject(6),
                        source.getObject(7),
                        source.getObject(8),
                        source.getObject(9),
                        source.getObject(10),
                        source.getObject(11),
                        source.getObject(12),
                        source.getObject(13),
                        source.getObject(14),
                        source.getObject(15),
                        source.getObject(16),
                        source.getObject(17),
                        source.getObject(18),
                        source.getObject(19),
                        source.getObject(20),
                        source.getObject(21),
                        source.getObject(22)
                );
            }
        }
    }

    private static int toIntValue(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
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

    private static SimpleResultSet createMatchResultsResultSet() {
        SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn("teamid", Types.BIGINT, 20, 0);
        resultSet.addColumn("wins", Types.INTEGER, 10, 0);
        resultSet.addColumn("draw", Types.INTEGER, 10, 0);
        resultSet.addColumn("lost", Types.INTEGER, 10, 0);
        resultSet.addColumn("points", Types.INTEGER, 10, 0);
        resultSet.addColumn("scored", Types.INTEGER, 10, 0);
        resultSet.addColumn("missed", Types.INTEGER, 10, 0);
        resultSet.addColumn("diff", Types.INTEGER, 10, 0);
        resultSet.addColumn("h_wins", Types.INTEGER, 10, 0);
        resultSet.addColumn("h_draw", Types.INTEGER, 10, 0);
        resultSet.addColumn("h_lost", Types.INTEGER, 10, 0);
        resultSet.addColumn("h_points", Types.INTEGER, 10, 0);
        resultSet.addColumn("h_scored", Types.INTEGER, 10, 0);
        resultSet.addColumn("h_missed", Types.INTEGER, 10, 0);
        resultSet.addColumn("h_diff", Types.INTEGER, 10, 0);
        resultSet.addColumn("g_wins", Types.INTEGER, 10, 0);
        resultSet.addColumn("g_draw", Types.INTEGER, 10, 0);
        resultSet.addColumn("g_lost", Types.INTEGER, 10, 0);
        resultSet.addColumn("g_points", Types.INTEGER, 10, 0);
        resultSet.addColumn("g_scored", Types.INTEGER, 10, 0);
        resultSet.addColumn("g_missed", Types.INTEGER, 10, 0);
        resultSet.addColumn("g_diff", Types.INTEGER, 10, 0);
        return resultSet;
    }

    private static SimpleResultSet createTournamentTeamsResultSet() {
        SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn("teamid", Types.BIGINT, 20, 0);
        resultSet.addColumn("teamname", Types.VARCHAR, 128, 0);
        resultSet.addColumn("teamlogo", Types.VARCHAR, 256, 0);
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

        private void applyResultRow(
                int wins,
                int draw,
                int lost,
                int points,
                int scored,
                int missed,
                int diff,
                int hWins,
                int hDraw,
                int hLost,
                int hPoints,
                int hScored,
                int hMissed,
                int hDiff,
                int gWins,
                int gDraw,
                int gLost,
                int gPoints,
                int gScored,
                int gMissed,
                int gDiff
        ) {
            this.hWins += hWins;
            this.hDraw += hDraw;
            this.hLost += hLost;
            this.hPoints += hPoints;
            this.hScored += hScored;
            this.hMissed += hMissed;
            this.hDiff += hDiff;
            this.hMatches += (hWins + hDraw + hLost);

            this.gWins += gWins;
            this.gDraw += gDraw;
            this.gLost += gLost;
            this.gPoints += gPoints;
            this.gScored += gScored;
            this.gMissed += gMissed;
            this.gDiff += gDiff;
            this.gMatches += (gWins + gDraw + gLost);
        }
    }
}
