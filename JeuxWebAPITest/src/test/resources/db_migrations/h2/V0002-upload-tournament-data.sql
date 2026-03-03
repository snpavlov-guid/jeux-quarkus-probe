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

INSERT INTO football."Team" (id, name, short_name, city, logo_url) VALUES
    (45, 'Dynamo Moscow', 'DYN', 'Moscow', NULL),
    (46, 'Rostov', 'ROS', 'Rostov-on-Don', NULL),
    (47, 'Rubin Kazan', 'RUB', 'Kazan', NULL),
    (48, 'Krasnodar', 'KRA', 'Krasnodar', NULL),
    (49, 'Akhmat Grozny', 'AKH', 'Grozny', NULL),
    (50, 'Ural Yekaterinburg', 'URA', 'Yekaterinburg', NULL),
    (51, 'Fakel Voronezh', 'FAK', 'Voronezh', NULL),
    (52, 'Krylia Sovetov', 'KRS', 'Samara', NULL),
    (53, 'Orenburg', 'ORE', 'Orenburg', NULL),
    (54, 'Nizhny Novgorod', 'NNV', 'Nizhny Novgorod', NULL),
    (55, 'Sochi', 'SOC', 'Sochi', NULL),
    (56, 'Baltika Kaliningrad', 'BAL', 'Kaliningrad', NULL);

INSERT INTO football."Tournament" (id, name, st_year, fn_year, league_id)
VALUES (36, 'RPL 2023-2024', 2023, 2024, 1);

INSERT INTO football."Stage" (id, name, "order", league_id, tournament_id)
VALUES (51, 'Main Stage', 1, 1, 36);

INSERT INTO football."Match" (
    id, tour, round, date, h_score, g_score, city, stadium,
    league_id, tournament_id, stage_id, h_team_id, g_team_id
) VALUES
    (3601, 1, 'R1', TIMESTAMP WITH TIME ZONE '2023-07-15 18:00:00+03:00', 2, 1, 'Moscow', 'Spartak Arena', 1, 36, 51, 41, 42),
    (3602, 2, 'R2', TIMESTAMP WITH TIME ZONE '2023-07-22 18:00:00+03:00', 1, 1, 'Saint Petersburg', 'Gazprom Arena', 1, 36, 51, 42, 41),
    (3603, 1, 'R1', TIMESTAMP WITH TIME ZONE '2023-07-15 20:00:00+03:00', 1, 0, 'Moscow', 'VEB Arena', 1, 36, 51, 43, 44),
    (3604, 2, 'R2', TIMESTAMP WITH TIME ZONE '2023-07-22 20:00:00+03:00', 0, 2, 'Moscow', 'RZD Arena', 1, 36, 51, 44, 43),
    (3605, 1, 'R1', TIMESTAMP WITH TIME ZONE '2023-07-16 16:00:00+03:00', 0, 0, 'Moscow', 'VTB Arena', 1, 36, 51, 45, 46),
    (3606, 2, 'R2', TIMESTAMP WITH TIME ZONE '2023-07-23 16:00:00+03:00', 2, 3, 'Rostov-on-Don', 'Rostov Arena', 1, 36, 51, 46, 45),
    (3607, 1, 'R1', TIMESTAMP WITH TIME ZONE '2023-07-16 18:30:00+03:00', 3, 1, 'Kazan', 'Ak Bars Arena', 1, 36, 51, 47, 48),
    (3608, 2, 'R2', TIMESTAMP WITH TIME ZONE '2023-07-23 18:30:00+03:00', 1, 2, 'Krasnodar', 'Krasnodar Stadium', 1, 36, 51, 48, 47),
    (3609, 1, 'R1', TIMESTAMP WITH TIME ZONE '2023-07-16 20:30:00+03:00', 1, 2, 'Grozny', 'Akhmat Arena', 1, 36, 51, 49, 50),
    (3610, 2, 'R2', TIMESTAMP WITH TIME ZONE '2023-07-23 20:30:00+03:00', 0, 0, 'Yekaterinburg', 'Central Stadium', 1, 36, 51, 50, 49),
    (3611, 1, 'R1', TIMESTAMP WITH TIME ZONE '2023-07-17 18:00:00+03:00', 2, 2, 'Voronezh', 'Fakel Stadium', 1, 36, 51, 51, 52),
    (3612, 2, 'R2', TIMESTAMP WITH TIME ZONE '2023-07-24 18:00:00+03:00', 1, 0, 'Samara', 'Solidarnost Arena', 1, 36, 51, 52, 51),
    (3613, 1, 'R1', TIMESTAMP WITH TIME ZONE '2023-07-17 20:00:00+03:00', 0, 1, 'Orenburg', 'Gazovik', 1, 36, 51, 53, 54),
    (3614, 2, 'R2', TIMESTAMP WITH TIME ZONE '2023-07-24 20:00:00+03:00', 2, 0, 'Nizhny Novgorod', 'Nizhny Novgorod Stadium', 1, 36, 51, 54, 53),
    (3615, 1, 'R1', TIMESTAMP WITH TIME ZONE '2023-07-18 19:00:00+03:00', 1, 0, 'Sochi', 'Fisht Stadium', 1, 36, 51, 55, 56),
    (3616, 2, 'R2', TIMESTAMP WITH TIME ZONE '2023-07-25 19:00:00+03:00', 3, 2, 'Kaliningrad', 'Rostec Arena', 1, 36, 51, 56, 55);

ALTER TABLE football."League" ALTER COLUMN id RESTART WITH 100;
ALTER TABLE football."Team" ALTER COLUMN id RESTART WITH 100;
ALTER TABLE football."Tournament" ALTER COLUMN id RESTART WITH 100;
ALTER TABLE football."Stage" ALTER COLUMN id RESTART WITH 100;
ALTER TABLE football."Match" ALTER COLUMN id RESTART WITH 1000;
