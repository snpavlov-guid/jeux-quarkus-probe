DROP FUNCTION IF EXISTS football."getHomeMatchResults"(bigint, bigint, bigint, character varying, bigint[]);

CREATE FUNCTION football."getHomeMatchResults"(
    leagueid bigint,
    tournamentid bigint,
    stageid bigint,
    tgroup character varying(32) DEFAULT NULL::character varying,
    teamids bigint[] DEFAULT NULL::bigint[]
)
RETURNS TABLE(
    teamid bigint,
    wins integer,
    draw integer,
    lost integer,
    points integer,
    scored integer,
    missed integer,
    diff integer,
    h_wins integer,
    h_draw integer,
    h_lost integer,
    h_points integer,
    h_scored integer,
    h_missed integer,
    h_diff integer,
    g_wins integer,
    g_draw integer,
    g_lost integer,
    g_points integer,
    g_scored integer,
    g_missed integer,
    g_diff integer
)
LANGUAGE plpgsql
AS $BODY$
BEGIN
    RETURN QUERY
    SELECT
        m.h_team_id AS teamid,
        CASE WHEN m.h_score > m.g_score THEN 1 ELSE 0 END AS wins,
        CASE WHEN m.h_score = m.g_score THEN 1 ELSE 0 END AS draw,
        CASE WHEN m.h_score < m.g_score THEN 1 ELSE 0 END AS lost,
        CASE
            WHEN m.h_score > m.g_score THEN COALESCE(t.win_pnt, 3)
            WHEN m.h_score = m.g_score THEN 1
            ELSE 0
        END AS points,
        m.h_score AS scored,
        m.g_score AS missed,
        (m.h_score - m.g_score) AS diff,
        CASE WHEN m.h_score > m.g_score THEN 1 ELSE 0 END AS h_wins,
        CASE WHEN m.h_score = m.g_score THEN 1 ELSE 0 END AS h_draw,
        CASE WHEN m.h_score < m.g_score THEN 1 ELSE 0 END AS h_lost,
        CASE
            WHEN m.h_score > m.g_score THEN COALESCE(t.win_pnt, 3)
            WHEN m.h_score = m.g_score THEN 1
            ELSE 0
        END AS h_points,
        m.h_score AS h_scored,
        m.g_score AS h_missed,
        (m.h_score - m.g_score) AS h_diff,
        0 AS g_wins,
        0 AS g_draw,
        0 AS g_lost,
        0 AS g_points,
        0 AS g_scored,
        0 AS g_missed,
        0 AS g_diff
    FROM football."Match" m
    JOIN football."Tournament" t
      ON t.id = m.tournament_id
     AND t.league_id = m.league_id
    WHERE m.league_id = leagueid
      AND m.tournament_id = tournamentid
      AND m.stage_id = stageid
      AND (tgroup IS NULL OR m."group" = tgroup)
      AND (
          teamids IS NULL
          OR cardinality(teamids) = 0
          OR (m.h_team_id = ANY(teamids) and m.g_team_id = ANY(teamids))
      )
    ORDER BY m.date, m.id;
END;
$BODY$;

ALTER FUNCTION football."getHomeMatchResults"(bigint, bigint, bigint, character varying, bigint[])
    OWNER TO postgres;

DROP FUNCTION IF EXISTS football."getGuestMatchResults"(bigint, bigint, bigint, character varying, bigint[]);

CREATE FUNCTION football."getGuestMatchResults"(
    leagueid bigint,
    tournamentid bigint,
    stageid bigint,
    tgroup character varying(32) DEFAULT NULL::character varying,
    teamids bigint[] DEFAULT NULL::bigint[]
)
RETURNS TABLE(
    teamid bigint,
    wins integer,
    draw integer,
    lost integer,
    points integer,
    scored integer,
    missed integer,
    diff integer,
    h_wins integer,
    h_draw integer,
    h_lost integer,
    h_points integer,
    h_scored integer,
    h_missed integer,
    h_diff integer,
    g_wins integer,
    g_draw integer,
    g_lost integer,
    g_points integer,
    g_scored integer,
    g_missed integer,
    g_diff integer
)
LANGUAGE plpgsql
AS $BODY$
BEGIN
    RETURN QUERY
    SELECT
        m.g_team_id AS teamid,
        CASE WHEN m.g_score > m.h_score THEN 1 ELSE 0 END AS wins,
        CASE WHEN m.g_score = m.h_score THEN 1 ELSE 0 END AS draw,
        CASE WHEN m.g_score < m.h_score THEN 1 ELSE 0 END AS lost,
        CASE
            WHEN m.g_score > m.h_score THEN COALESCE(t.win_pnt, 3)
            WHEN m.g_score = m.h_score THEN 1
            ELSE 0
        END AS points,
        m.g_score AS scored,
        m.h_score AS missed,
        (m.g_score - m.h_score) AS diff,
        0 AS h_wins,
        0 AS h_draw,
        0 AS h_lost,
        0 AS h_points,
        0 AS h_scored,
        0 AS h_missed,
        0 AS h_diff,
        CASE WHEN m.g_score > m.h_score THEN 1 ELSE 0 END AS g_wins,
        CASE WHEN m.g_score = m.h_score THEN 1 ELSE 0 END AS g_draw,
        CASE WHEN m.g_score < m.h_score THEN 1 ELSE 0 END AS g_lost,
        CASE
            WHEN m.g_score > m.h_score THEN COALESCE(t.win_pnt, 3)
            WHEN m.g_score = m.h_score THEN 1
            ELSE 0
        END AS g_points,
        m.g_score AS g_scored,
        m.h_score AS g_missed,
        (m.g_score - m.h_score) AS g_diff
    FROM football."Match" m
    JOIN football."Tournament" t
      ON t.id = m.tournament_id
     AND t.league_id = m.league_id
    WHERE m.league_id = leagueid
      AND m.tournament_id = tournamentid
      AND m.stage_id = stageid
      AND (tgroup IS NULL OR m."group" = tgroup)
      AND (
          teamids IS NULL
          OR cardinality(teamids) = 0
          OR (m.g_team_id = ANY(teamids) and m.h_team_id = ANY(teamids))
      )
    ORDER BY m.date, m.id;
END;
$BODY$;

ALTER FUNCTION football."getGuestMatchResults"(bigint, bigint, bigint, character varying, bigint[])
    OWNER TO postgres;

DROP FUNCTION IF EXISTS football."getAllMatchResults"(bigint, bigint, bigint, character varying, bigint[], integer);

CREATE FUNCTION football."getAllMatchResults"(
    leagueid bigint,
    tournamentid bigint,
    stageid bigint,
    tgroup character varying(32) DEFAULT NULL::character varying,
    teamids bigint[] DEFAULT NULL::bigint[],
    matchtype integer DEFAULT NULL::integer
)
RETURNS TABLE(
    teamid bigint,
    wins integer,
    draw integer,
    lost integer,
    points integer,
    scored integer,
    missed integer,
    diff integer,
    h_wins integer,
    h_draw integer,
    h_lost integer,
    h_points integer,
    h_scored integer,
    h_missed integer,
    h_diff integer,
    g_wins integer,
    g_draw integer,
    g_lost integer,
    g_points integer,
    g_scored integer,
    g_missed integer,
    g_diff integer
)
LANGUAGE plpgsql
AS $BODY$
BEGIN
    IF matchtype = 1 THEN
        RETURN QUERY
        SELECT *
        FROM football."getHomeMatchResults"(leagueid, tournamentid, stageid, tgroup, teamids);
    ELSIF matchtype = -1 THEN
        RETURN QUERY
        SELECT *
        FROM football."getGuestMatchResults"(leagueid, tournamentid, stageid, tgroup, teamids);
    ELSE
        RETURN QUERY
        SELECT *
        FROM football."getHomeMatchResults"(leagueid, tournamentid, stageid, tgroup, teamids)
        UNION ALL
        SELECT *
        FROM football."getGuestMatchResults"(leagueid, tournamentid, stageid, tgroup, teamids);
    END IF;
END;
$BODY$;

ALTER FUNCTION football."getAllMatchResults"(bigint, bigint, bigint, character varying, bigint[], integer)
    OWNER TO postgres;

DROP FUNCTION IF EXISTS football."getTournamentTeams"(bigint, bigint, bigint, character varying);

CREATE FUNCTION football."getTournamentTeams"(
    leagueid bigint,
    tournamentid bigint,
    stageid bigint,
    tgroup character varying(32) DEFAULT NULL::character varying
)
RETURNS TABLE(
    teamid bigint,
    teamname character varying,
    teamlogo character varying
)
LANGUAGE plpgsql
AS $BODY$
BEGIN
    RETURN QUERY
    WITH teamids AS (
        SELECT m.h_team_id AS teamid
        FROM football."Match" m
        WHERE m.league_id = leagueid
          AND m.tournament_id = tournamentid
          AND m.stage_id = stageid
          AND (tgroup IS NULL OR m."group" = tgroup)
        UNION
        SELECT m.g_team_id AS teamid
        FROM football."Match" m
        WHERE m.league_id = leagueid
          AND m.tournament_id = tournamentid
          AND m.stage_id = stageid
          AND (tgroup IS NULL OR m."group" = tgroup)
    )
    SELECT
        t.id AS teamid,
        t.name AS teamname,
        t.logo_url AS teamlogo
    FROM teamids ti
    JOIN football."Team" t ON t.id = ti.teamid
    ORDER BY t.name;
END;
$BODY$;

ALTER FUNCTION football."getTournamentTeams"(bigint, bigint, bigint, character varying)
    OWNER TO postgres;

DROP FUNCTION IF EXISTS football."getTournamentStandingsEx"(bigint, bigint, bigint, character varying, bigint[], integer);
DROP FUNCTION IF EXISTS football."getTournamentStandingsEx"(bigint, bigint, bigint, character varying, bigint[], integer, bigint, integer);

CREATE FUNCTION football."getTournamentStandingsEx"(
    leagueid bigint,
    tournamentid bigint,
    stageid bigint,
    tgroup character varying(32) DEFAULT NULL::character varying,
    teamids bigint[] DEFAULT NULL::bigint[],
    matchtype integer DEFAULT NULL::integer,
    prevstageid bigint DEFAULT NULL::bigint,
    prevplays integer DEFAULT NULL::integer
)
RETURNS TABLE(
    teamid bigint,
    teamname character varying,
    teamlogo character varying,
    matches integer,
    wins integer,
    draw integer,
    lost integer,
    points integer,
    scored integer,
    missed integer,
    diff integer,
    h_matches integer,
    h_wins integer,
    h_draw integer,
    h_lost integer,
    h_points integer,
    h_scored integer,
    h_missed integer,
    h_diff integer,
    g_matches integer,
    g_wins integer,
    g_draw integer,
    g_lost integer,
    g_points integer,
    g_scored integer,
    g_missed integer,
    g_diff integer
)
LANGUAGE plpgsql
AS $BODY$
BEGIN
    RETURN QUERY
    WITH all_results AS (
        SELECT *
        FROM football."getAllMatchResults"(leagueid, tournamentid, stageid, tgroup, teamids, matchtype)
        UNION ALL
        SELECT *
        FROM football."getAllMatchResults"(
            leagueid,
            tournamentid,
            prevstageid,
            NULL::character varying,
            CASE
                WHEN prevplays = 1 THEN (
                    SELECT COALESCE(array_agg(tt.teamid), ARRAY[]::bigint[])
                    FROM football."getTournamentTeams"(leagueid, tournamentid, stageid, tgroup) tt
                )
                ELSE teamids
            END,
            matchtype
        )
        WHERE prevstageid IS NOT NULL
    ),
    current_teams AS (
        SELECT tt.teamid
        FROM football."getTournamentTeams"(leagueid, tournamentid, stageid, tgroup) tt
    ),
    aggregated AS (
        SELECT
            r.teamid,
            SUM(r.wins)::integer AS wins,
            SUM(r.draw)::integer AS draw,
            SUM(r.lost)::integer AS lost,
            SUM(r.points)::integer AS points,
            SUM(r.scored)::integer AS scored,
            SUM(r.missed)::integer AS missed,
            SUM(r.diff)::integer AS diff,
            SUM(r.h_wins)::integer AS h_wins,
            SUM(r.h_draw)::integer AS h_draw,
            SUM(r.h_lost)::integer AS h_lost,
            SUM(r.h_points)::integer AS h_points,
            SUM(r.h_scored)::integer AS h_scored,
            SUM(r.h_missed)::integer AS h_missed,
            SUM(r.h_diff)::integer AS h_diff,
            SUM(r.g_wins)::integer AS g_wins,
            SUM(r.g_draw)::integer AS g_draw,
            SUM(r.g_lost)::integer AS g_lost,
            SUM(r.g_points)::integer AS g_points,
            SUM(r.g_scored)::integer AS g_scored,
            SUM(r.g_missed)::integer AS g_missed,
            SUM(r.g_diff)::integer AS g_diff
        FROM all_results r
        WHERE prevstageid IS NULL
           OR r.teamid IN (SELECT ct.teamid FROM current_teams ct)
        GROUP BY r.teamid
    )
    SELECT
        tm.id AS teamid,
        tm.name AS teamname,
        tm.logo_url AS teamlogo,
        (a.wins + a.draw + a.lost)::integer AS matches,
        a.wins,
        a.draw,
        a.lost,
        a.points,
        a.scored,
        a.missed,
        a.diff,
        (a.h_wins + a.h_draw + a.h_lost)::integer AS h_matches,
        a.h_wins,
        a.h_draw,
        a.h_lost,
        a.h_points,
        a.h_scored,
        a.h_missed,
        a.h_diff,
        (a.g_wins + a.g_draw + a.g_lost)::integer AS g_matches,
        a.g_wins,
        a.g_draw,
        a.g_lost,
        a.g_points,
        a.g_scored,
        a.g_missed,
        a.g_diff
    FROM aggregated a
    JOIN football."Team" tm ON tm.id = a.teamid
    ORDER BY a.points DESC, a.wins DESC, a.diff DESC, a.scored DESC, a.g_wins DESC, a.g_scored DESC;
END;
$BODY$;

ALTER FUNCTION football."getTournamentStandingsEx"(bigint, bigint, bigint, character varying, bigint[], integer, bigint, integer)
    OWNER TO postgres;
