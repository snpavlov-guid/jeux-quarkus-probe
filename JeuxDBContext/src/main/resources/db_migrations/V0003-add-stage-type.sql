ALTER TABLE football."Stage"
    ADD COLUMN stage_type character varying(16);

UPDATE football."Stage"
SET stage_type = CASE
    WHEN name IN ('Понижение/повышение - финал', 'Золотой матч') THEN 'EXTRAPLAY'
    ELSE 'REGULAR'
END;

ALTER TABLE football."Stage"
    ALTER COLUMN stage_type SET NOT NULL;

-- FUNCTION: football.getTournamentStandings(bigint, bigint, bigint)

DROP FUNCTION IF EXISTS football."getTournamentStandings"(bigint, bigint, bigint);

CREATE FUNCTION football."getTournamentStandings"(
	leagueid bigint,
	tournamentid bigint,
	stageid bigint)
    RETURNS TABLE(teamid bigint, teamname character varying, teamlogo character varying, matches integer, wins integer, draw integer, lost integer, points integer, scored integer, missed integer, diff integer, h_matches integer, h_wins integer, h_draw integer, h_lost integer, h_points integer, h_scored integer, h_missed integer, h_diff integer, g_matches integer, g_wins integer, g_draw integer, g_lost integer, g_points integer, g_scored integer, g_missed integer, g_diff integer) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
begin
return query
select tm.id as teamid 
, tm.name as teamname
, tm.logo_url as teamlogo 

, home_matches.h_matches + guest_matches.g_matches as "matches"
, home_matches.h_wins + guest_matches.g_wins as "wins"
, home_matches.h_draw + guest_matches.g_draw as "draw"
, home_matches.h_lost + guest_matches.g_lost as "lost"
, home_matches.h_points + guest_matches.g_points as "points"
, home_matches.h_scored + guest_matches.g_scored as "scored"
, home_matches.h_missed + guest_matches.g_missed as "missed"
, home_matches.h_diff + guest_matches.g_diff as "diff"

, home_matches.h_matches
, home_matches.h_wins
, home_matches.h_draw
, home_matches.h_lost
, home_matches.h_points
, home_matches.h_scored
, home_matches.h_missed
, home_matches.h_diff

, guest_matches.g_matches
, guest_matches.g_wins
, guest_matches.g_draw
, guest_matches.g_lost
, guest_matches.g_points
, guest_matches.g_scored
, guest_matches.g_missed
, guest_matches.g_diff

from (select m.h_team_id as teamid
, count(*)::integer as "h_matches"
, count(*) FILTER (WHERE h_score > g_score)::integer as "h_wins"
, count(*) FILTER (WHERE h_score = g_score)::integer as "h_draw"
, count(*) FILTER (WHERE h_score < g_score)::integer as "h_lost"
, (3 * (count(*) FILTER (WHERE h_score > g_score)) +
   1 * (count(*) FILTER (WHERE h_score = g_score)) )::integer AS "h_points"
, sum(h_score)::integer as "h_scored"
, sum(g_score)::integer as "h_missed"
, (sum(h_score) - sum(g_score))::integer as "h_diff"
from football."Match" m
where m.league_id = leagueId and m.tournament_id = tournamentId and m.stage_id = stageId
group by m.h_team_id) home_matches

join (select m.g_team_id as teamid
, count(*)::integer as "g_matches"
, count(*) FILTER (WHERE h_score < g_score)::integer as "g_wins"
, count(*) FILTER (WHERE h_score = g_score)::integer as "g_draw"
, count(*) FILTER (WHERE h_score > g_score)::integer as "g_lost"
, (3 * (count(*) FILTER (WHERE h_score < g_score)) +
   1 * (count(*) FILTER (WHERE h_score = g_score)) )::integer AS "g_points"
, sum(g_score)::integer as "g_scored"
, sum(h_score)::integer as "g_missed"
, (sum(g_score) - sum(h_score))::integer as "g_diff"
from football."Match" m
where m.league_id = leagueId and m.tournament_id = tournamentId and m.stage_id = stageId
group by m.g_team_id) guest_matches on home_matches.teamid = guest_matches.teamid

join football."Team" tm on home_matches.teamid = tm.id

order by "points" desc, "wins" desc, "diff" desc, "scored" desc, "g_wins" desc, "g_scored" desc;

end;
$BODY$;

ALTER FUNCTION football."getTournamentStandings"(bigint, bigint, bigint)
    OWNER TO postgres;
    