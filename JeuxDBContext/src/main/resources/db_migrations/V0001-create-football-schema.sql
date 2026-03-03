--
-- PostgreSQL database dump
--

-- Dumped from database version 14.0
-- Dumped by pg_dump version 14.0

-- Started on 2026-03-02 22:49:46

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 4 (class 2615 OID 1260249)
-- Name: football; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA football;


ALTER SCHEMA football OWNER TO postgres;

--
-- TOC entry 231 (class 1255 OID 1275878)
-- Name: getTournamentStandings(bigint, bigint, bigint); Type: FUNCTION; Schema: football; Owner: postgres
--

CREATE FUNCTION football."getTournamentStandings"(leagueid bigint, tournamentid bigint, stageid bigint) RETURNS TABLE(teamid bigint, teamname character varying, matches integer, wins integer, draw integer, lost integer, points integer, scored integer, missed integer, diff integer, h_matches integer, h_wins integer, h_draw integer, h_lost integer, h_points integer, h_scored integer, h_missed integer, h_diff integer, g_matches integer, g_wins integer, g_draw integer, g_lost integer, g_points integer, g_scored integer, g_missed integer, g_diff integer)
    LANGUAGE plpgsql
    AS $$
begin
return query
select tm.id as teamid 
, tm.name as teamname

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
$$;


ALTER FUNCTION football."getTournamentStandings"(leagueid bigint, tournamentid bigint, stageid bigint) OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 211 (class 1259 OID 1260455)
-- Name: League; Type: TABLE; Schema: football; Owner: postgres
--

CREATE TABLE football."League" (
    id bigint NOT NULL,
    name character varying(128) NOT NULL
);


ALTER TABLE football."League" OWNER TO postgres;

--
-- TOC entry 210 (class 1259 OID 1260454)
-- Name: League_id_seq; Type: SEQUENCE; Schema: football; Owner: postgres
--

CREATE SEQUENCE football."League_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE football."League_id_seq" OWNER TO postgres;

--
-- TOC entry 3385 (class 0 OID 0)
-- Dependencies: 210
-- Name: League_id_seq; Type: SEQUENCE OWNED BY; Schema: football; Owner: postgres
--

ALTER SEQUENCE football."League_id_seq" OWNED BY football."League".id;


--
-- TOC entry 213 (class 1259 OID 1260462)
-- Name: Match; Type: TABLE; Schema: football; Owner: postgres
--

CREATE TABLE football."Match" (
    g_score integer NOT NULL,
    h_score integer NOT NULL,
    tour integer NOT NULL,
    date timestamp(6) with time zone,
    g_team_id bigint NOT NULL,
    h_team_id bigint NOT NULL,
    id bigint NOT NULL,
    league_id bigint NOT NULL,
    stage_id bigint NOT NULL,
    tournament_id bigint NOT NULL,
    round character varying(16) NOT NULL,
    city character varying(128),
    stadium character varying(128)
);


ALTER TABLE football."Match" OWNER TO postgres;

--
-- TOC entry 212 (class 1259 OID 1260461)
-- Name: Match_id_seq; Type: SEQUENCE; Schema: football; Owner: postgres
--

CREATE SEQUENCE football."Match_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE football."Match_id_seq" OWNER TO postgres;

--
-- TOC entry 3386 (class 0 OID 0)
-- Dependencies: 212
-- Name: Match_id_seq; Type: SEQUENCE OWNED BY; Schema: football; Owner: postgres
--

ALTER SEQUENCE football."Match_id_seq" OWNED BY football."Match".id;


--
-- TOC entry 215 (class 1259 OID 1260469)
-- Name: Stage; Type: TABLE; Schema: football; Owner: postgres
--

CREATE TABLE football."Stage" (
    "order" integer NOT NULL,
    id bigint NOT NULL,
    league_id bigint NOT NULL,
    tournament_id bigint NOT NULL,
    name character varying(128) NOT NULL
);


ALTER TABLE football."Stage" OWNER TO postgres;

--
-- TOC entry 214 (class 1259 OID 1260468)
-- Name: Stage_id_seq; Type: SEQUENCE; Schema: football; Owner: postgres
--

CREATE SEQUENCE football."Stage_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE football."Stage_id_seq" OWNER TO postgres;

--
-- TOC entry 3387 (class 0 OID 0)
-- Dependencies: 214
-- Name: Stage_id_seq; Type: SEQUENCE OWNED BY; Schema: football; Owner: postgres
--

ALTER SEQUENCE football."Stage_id_seq" OWNED BY football."Stage".id;


--
-- TOC entry 217 (class 1259 OID 1260476)
-- Name: Team; Type: TABLE; Schema: football; Owner: postgres
--

CREATE TABLE football."Team" (
    short_name character varying(6) NOT NULL,
    id bigint NOT NULL,
    city character varying(128),
    name character varying(128) NOT NULL,
    logo_url character varying(256)
);


ALTER TABLE football."Team" OWNER TO postgres;

--
-- TOC entry 216 (class 1259 OID 1260475)
-- Name: Team_id_seq; Type: SEQUENCE; Schema: football; Owner: postgres
--

CREATE SEQUENCE football."Team_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE football."Team_id_seq" OWNER TO postgres;

--
-- TOC entry 3388 (class 0 OID 0)
-- Dependencies: 216
-- Name: Team_id_seq; Type: SEQUENCE OWNED BY; Schema: football; Owner: postgres
--

ALTER SEQUENCE football."Team_id_seq" OWNED BY football."Team".id;


--
-- TOC entry 219 (class 1259 OID 1260485)
-- Name: Tournament; Type: TABLE; Schema: football; Owner: postgres
--

CREATE TABLE football."Tournament" (
    fn_year integer,
    st_year integer NOT NULL,
    id bigint NOT NULL,
    league_id bigint NOT NULL,
    name character varying(128) NOT NULL
);


ALTER TABLE football."Tournament" OWNER TO postgres;

--
-- TOC entry 218 (class 1259 OID 1260484)
-- Name: Tournament_id_seq; Type: SEQUENCE; Schema: football; Owner: postgres
--

CREATE SEQUENCE football."Tournament_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE football."Tournament_id_seq" OWNER TO postgres;

--
-- TOC entry 3389 (class 0 OID 0)
-- Dependencies: 218
-- Name: Tournament_id_seq; Type: SEQUENCE OWNED BY; Schema: football; Owner: postgres
--

ALTER SEQUENCE football."Tournament_id_seq" OWNED BY football."Tournament".id;


--
-- TOC entry 3186 (class 2604 OID 1260458)
-- Name: League id; Type: DEFAULT; Schema: football; Owner: postgres
--

ALTER TABLE ONLY football."League" ALTER COLUMN id SET DEFAULT nextval('football."League_id_seq"'::regclass);


--
-- TOC entry 3187 (class 2604 OID 1260465)
-- Name: Match id; Type: DEFAULT; Schema: football; Owner: postgres
--

ALTER TABLE ONLY football."Match" ALTER COLUMN id SET DEFAULT nextval('football."Match_id_seq"'::regclass);


--
-- TOC entry 3188 (class 2604 OID 1260472)
-- Name: Stage id; Type: DEFAULT; Schema: football; Owner: postgres
--

ALTER TABLE ONLY football."Stage" ALTER COLUMN id SET DEFAULT nextval('football."Stage_id_seq"'::regclass);


--
-- TOC entry 3189 (class 2604 OID 1260479)
-- Name: Team id; Type: DEFAULT; Schema: football; Owner: postgres
--

ALTER TABLE ONLY football."Team" ALTER COLUMN id SET DEFAULT nextval('football."Team_id_seq"'::regclass);


--
-- TOC entry 3190 (class 2604 OID 1260488)
-- Name: Tournament id; Type: DEFAULT; Schema: football; Owner: postgres
--

ALTER TABLE ONLY football."Tournament" ALTER COLUMN id SET DEFAULT nextval('football."Tournament_id_seq"'::regclass);



--
-- TOC entry 3192 (class 2606 OID 1260460)
-- Name: League League_pkey; Type: CONSTRAINT; Schema: football; Owner: postgres
--

ALTER TABLE ONLY football."League"
    ADD CONSTRAINT "League_pkey" PRIMARY KEY (id);


--
-- TOC entry 3195 (class 2606 OID 1260467)
-- Name: Match Match_pkey; Type: CONSTRAINT; Schema: football; Owner: postgres
--

ALTER TABLE ONLY football."Match"
    ADD CONSTRAINT "Match_pkey" PRIMARY KEY (id);


--
-- TOC entry 3207 (class 2606 OID 1260474)
-- Name: Stage Stage_pkey; Type: CONSTRAINT; Schema: football; Owner: postgres
--

ALTER TABLE ONLY football."Stage"
    ADD CONSTRAINT "Stage_pkey" PRIMARY KEY (id);


--
-- TOC entry 3213 (class 2606 OID 1260483)
-- Name: Team Team_pkey; Type: CONSTRAINT; Schema: football; Owner: postgres
--

ALTER TABLE ONLY football."Team"
    ADD CONSTRAINT "Team_pkey" PRIMARY KEY (id);


--
-- TOC entry 3218 (class 2606 OID 1260490)
-- Name: Tournament Tournament_pkey; Type: CONSTRAINT; Schema: football; Owner: postgres
--

ALTER TABLE ONLY football."Tournament"
    ADD CONSTRAINT "Tournament_pkey" PRIMARY KEY (id);


--
-- TOC entry 3193 (class 1259 OID 1260491)
-- Name: idx_league_name; Type: INDEX; Schema: football; Owner: postgres
--

CREATE INDEX idx_league_name ON football."League" USING btree (name);


--
-- TOC entry 3196 (class 1259 OID 1260499)
-- Name: idx_match_date; Type: INDEX; Schema: football; Owner: postgres
--

CREATE INDEX idx_match_date ON football."Match" USING btree (date);


--
-- TOC entry 3197 (class 1259 OID 1260501)
-- Name: idx_match_g_score; Type: INDEX; Schema: football; Owner: postgres
--

CREATE INDEX idx_match_g_score ON football."Match" USING btree (g_score);


--
-- TOC entry 3198 (class 1259 OID 1260496)
-- Name: idx_match_g_team_id; Type: INDEX; Schema: football; Owner: postgres
--

CREATE INDEX idx_match_g_team_id ON football."Match" USING btree (g_team_id);


--
-- TOC entry 3199 (class 1259 OID 1260500)
-- Name: idx_match_h_score; Type: INDEX; Schema: football; Owner: postgres
--

CREATE INDEX idx_match_h_score ON football."Match" USING btree (h_score);


--
-- TOC entry 3200 (class 1259 OID 1260495)
-- Name: idx_match_h_team_id; Type: INDEX; Schema: football; Owner: postgres
--

CREATE INDEX idx_match_h_team_id ON football."Match" USING btree (h_team_id);


--
-- TOC entry 3201 (class 1259 OID 1260492)
-- Name: idx_match_league_id; Type: INDEX; Schema: football; Owner: postgres
--

CREATE INDEX idx_match_league_id ON football."Match" USING btree (league_id);


--
-- TOC entry 3202 (class 1259 OID 1260498)
-- Name: idx_match_round; Type: INDEX; Schema: football; Owner: postgres
--

CREATE INDEX idx_match_round ON football."Match" USING btree (round);


--
-- TOC entry 3203 (class 1259 OID 1260494)
-- Name: idx_match_stage_id; Type: INDEX; Schema: football; Owner: postgres
--

CREATE INDEX idx_match_stage_id ON football."Match" USING btree (stage_id);


--
-- TOC entry 3204 (class 1259 OID 1260497)
-- Name: idx_match_tour; Type: INDEX; Schema: football; Owner: postgres
--

CREATE INDEX idx_match_tour ON football."Match" USING btree (tour);


--
-- TOC entry 3205 (class 1259 OID 1260493)
-- Name: idx_match_tournament_id; Type: INDEX; Schema: football; Owner: postgres
--

CREATE INDEX idx_match_tournament_id ON football."Match" USING btree (tournament_id);


--
-- TOC entry 3208 (class 1259 OID 1260502)
-- Name: idx_stage_league_id; Type: INDEX; Schema: football; Owner: postgres
--

CREATE INDEX idx_stage_league_id ON football."Stage" USING btree (league_id);


--
-- TOC entry 3209 (class 1259 OID 1260505)
-- Name: idx_stage_name; Type: INDEX; Schema: football; Owner: postgres
--

CREATE INDEX idx_stage_name ON football."Stage" USING btree (name);


--
-- TOC entry 3210 (class 1259 OID 1260504)
-- Name: idx_stage_order; Type: INDEX; Schema: football; Owner: postgres
--

CREATE INDEX idx_stage_order ON football."Stage" USING btree ("order");


--
-- TOC entry 3211 (class 1259 OID 1260503)
-- Name: idx_stage_tournament_id; Type: INDEX; Schema: football; Owner: postgres
--

CREATE INDEX idx_stage_tournament_id ON football."Stage" USING btree (tournament_id);


--
-- TOC entry 3214 (class 1259 OID 1260508)
-- Name: idx_team_city; Type: INDEX; Schema: football; Owner: postgres
--

CREATE INDEX idx_team_city ON football."Team" USING btree (city);


--
-- TOC entry 3215 (class 1259 OID 1260506)
-- Name: idx_team_name; Type: INDEX; Schema: football; Owner: postgres
--

CREATE INDEX idx_team_name ON football."Team" USING btree (name);


--
-- TOC entry 3216 (class 1259 OID 1260507)
-- Name: idx_team_short_name; Type: INDEX; Schema: football; Owner: postgres
--

CREATE INDEX idx_team_short_name ON football."Team" USING btree (short_name);


--
-- TOC entry 3219 (class 1259 OID 1260511)
-- Name: idx_tournament_fn_year; Type: INDEX; Schema: football; Owner: postgres
--

CREATE INDEX idx_tournament_fn_year ON football."Tournament" USING btree (fn_year);


--
-- TOC entry 3220 (class 1259 OID 1260509)
-- Name: idx_tournament_league_id; Type: INDEX; Schema: football; Owner: postgres
--

CREATE INDEX idx_tournament_league_id ON football."Tournament" USING btree (league_id);


--
-- TOC entry 3221 (class 1259 OID 1260512)
-- Name: idx_tournament_name; Type: INDEX; Schema: football; Owner: postgres
--

CREATE INDEX idx_tournament_name ON football."Tournament" USING btree (name);


--
-- TOC entry 3222 (class 1259 OID 1260510)
-- Name: idx_tournament_st_year; Type: INDEX; Schema: football; Owner: postgres
--

CREATE INDEX idx_tournament_st_year ON football."Tournament" USING btree (st_year);


--
-- TOC entry 3223 (class 2606 OID 1260513)
-- Name: Match fk_match_g_team_id; Type: FK CONSTRAINT; Schema: football; Owner: postgres
--

ALTER TABLE ONLY football."Match"
    ADD CONSTRAINT fk_match_g_team_id FOREIGN KEY (g_team_id) REFERENCES football."Team"(id);


--
-- TOC entry 3224 (class 2606 OID 1260518)
-- Name: Match fk_match_h_team_id; Type: FK CONSTRAINT; Schema: football; Owner: postgres
--

ALTER TABLE ONLY football."Match"
    ADD CONSTRAINT fk_match_h_team_id FOREIGN KEY (h_team_id) REFERENCES football."Team"(id);


--
-- TOC entry 3225 (class 2606 OID 1260523)
-- Name: Match fk_match_league_id; Type: FK CONSTRAINT; Schema: football; Owner: postgres
--

ALTER TABLE ONLY football."Match"
    ADD CONSTRAINT fk_match_league_id FOREIGN KEY (league_id) REFERENCES football."League"(id) ON DELETE CASCADE;


--
-- TOC entry 3226 (class 2606 OID 1260528)
-- Name: Match fk_match_stage_id; Type: FK CONSTRAINT; Schema: football; Owner: postgres
--

ALTER TABLE ONLY football."Match"
    ADD CONSTRAINT fk_match_stage_id FOREIGN KEY (stage_id) REFERENCES football."Stage"(id) ON DELETE CASCADE;


--
-- TOC entry 3227 (class 2606 OID 1260533)
-- Name: Match fk_match_tournament_id; Type: FK CONSTRAINT; Schema: football; Owner: postgres
--

ALTER TABLE ONLY football."Match"
    ADD CONSTRAINT fk_match_tournament_id FOREIGN KEY (tournament_id) REFERENCES football."Tournament"(id) ON DELETE CASCADE;


--
-- TOC entry 3228 (class 2606 OID 1260538)
-- Name: Stage fk_stage_league_id; Type: FK CONSTRAINT; Schema: football; Owner: postgres
--

ALTER TABLE ONLY football."Stage"
    ADD CONSTRAINT fk_stage_league_id FOREIGN KEY (league_id) REFERENCES football."League"(id) ON DELETE CASCADE;


--
-- TOC entry 3229 (class 2606 OID 1260543)
-- Name: Stage fk_stage_tournament_id; Type: FK CONSTRAINT; Schema: football; Owner: postgres
--

ALTER TABLE ONLY football."Stage"
    ADD CONSTRAINT fk_stage_tournament_id FOREIGN KEY (tournament_id) REFERENCES football."Tournament"(id) ON DELETE CASCADE;


--
-- TOC entry 3230 (class 2606 OID 1260548)
-- Name: Tournament fk_tournament_league_id; Type: FK CONSTRAINT; Schema: football; Owner: postgres
--

ALTER TABLE ONLY football."Tournament"
    ADD CONSTRAINT fk_tournament_league_id FOREIGN KEY (league_id) REFERENCES football."League"(id) ON DELETE CASCADE;


-- Completed on 2026-03-02 22:49:47

--
-- PostgreSQL database dump complete
--

