INSERT INTO football."League" (id, name)
VALUES (1, 'Russian Premier League');

INSERT INTO football."Team" (id, name, short_name, city, logo_url) VALUES
    (41, 'Spartak Moscow', 'SPM', 'Moscow', NULL),
    (42, 'Zenit Saint Petersburg', 'ZEN', 'Saint Petersburg', NULL),
    (43, 'CSKA Moscow', 'CSK', 'Moscow', NULL),
    (44, 'Lokomotiv Moscow', 'LOK', 'Moscow', NULL);

INSERT INTO football."Tournament" (id, name, st_year, fn_year, league_id)
VALUES (12, 'RPL 2020-2021', 2020, 2021, 1);

INSERT INTO football."Stage" (id, name, "order", league_id, tournament_id)
VALUES (22, 'Main Stage', 1, 1, 12);

INSERT INTO football."Match" (
    id, tour, round, date, h_score, g_score, city, stadium,
    league_id, tournament_id, stage_id, h_team_id, g_team_id
) VALUES (
    1705, 1, 'R1', TIMESTAMP WITH TIME ZONE '2020-08-12 00:00:00+03:00', 3, 0, 'Moscow', 'Otkritie Arena',
    1, 12, 22, 41, 42
);

ALTER TABLE football."League" ALTER COLUMN id RESTART WITH 100;
ALTER TABLE football."Team" ALTER COLUMN id RESTART WITH 100;
ALTER TABLE football."Tournament" ALTER COLUMN id RESTART WITH 100;
ALTER TABLE football."Stage" ALTER COLUMN id RESTART WITH 100;
ALTER TABLE football."Match" ALTER COLUMN id RESTART WITH 1000;
