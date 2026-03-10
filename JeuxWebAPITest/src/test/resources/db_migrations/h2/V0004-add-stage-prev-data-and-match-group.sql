ALTER TABLE football."Stage"
    ADD COLUMN groups VARCHAR(32) ARRAY;

ALTER TABLE football."Stage"
    ADD COLUMN prev_stage_id BIGINT;

ALTER TABLE football."Stage"
    ADD COLUMN prev_plays VARCHAR(16);

ALTER TABLE football."Stage"
    ADD CONSTRAINT fk_stage_prev_stage_id
    FOREIGN KEY (prev_stage_id) REFERENCES football."Stage"(id);

ALTER TABLE football."Match"
    ADD COLUMN "group" VARCHAR(32);

CREATE INDEX idx_match_group ON football."Match"("group");

ALTER TABLE football."Tournament"
    ADD COLUMN win_pnt INTEGER;

UPDATE football."Tournament"
SET win_pnt = 2
WHERE id IN (1, 2, 3);

UPDATE football."Tournament"
SET win_pnt = 3
WHERE win_pnt IS NULL;

ALTER TABLE football."Tournament"
    ALTER COLUMN win_pnt SET DEFAULT 3;

ALTER TABLE football."Tournament"
    ALTER COLUMN win_pnt SET NOT NULL;

UPDATE football."Stage"
SET groups = ARRAY['A', 'B']
WHERE league_id = 1 AND tournament_id = 12 AND id = 22;

UPDATE football."Stage"
SET prev_plays = 'SAMETEAMS',
    prev_stage_id = 22
WHERE league_id = 1 AND tournament_id = 12 AND id IN (23, 24);

UPDATE football."Stage"
SET groups = ARRAY['A', 'B'],
    prev_stage_id = 46,
    prev_plays = 'ALLPLAYS'
WHERE league_id = 1 AND tournament_id = 31 AND id = 45;

UPDATE football."Match" m
SET "group" = 'A'
WHERE m.league_id = 1
  AND m.tournament_id = 12
  AND m.stage_id = 22
  AND (
      m.h_team_id IN (
          SELECT t.id
          FROM football."Team" t
          WHERE t.name IN (
              'Динамо Москва',
              'Локомотив Москва',
              'Алания Владикавказ',
              'ЦСКА Москва',
              'Текстильщик Камышин',
              'Урал',
              'Океан',
              'Факел',
              'Динамо Ств',
              'Тюмень'
          )
      )
      OR m.g_team_id IN (
          SELECT t.id
          FROM football."Team" t
          WHERE t.name IN (
              'Динамо Москва',
              'Локомотив Москва',
              'Алания Владикавказ',
              'ЦСКА Москва',
              'Текстильщик Камышин',
              'Урал',
              'Океан',
              'Факел',
              'Динамо Ставрополь',
              'Тюмень'
          )
      )
  );

UPDATE football."Match"
SET "group" = 'B'
WHERE league_id = 1
  AND tournament_id = 12
  AND stage_id = 22
  AND "group" IS NULL;

UPDATE football."Match" m
SET "group" = 'A'
WHERE m.league_id = 1
  AND m.tournament_id = 31
  AND m.stage_id = 45
  AND (
      m.h_team_id IN (
          SELECT t.id
          FROM football."Team" t
          WHERE t.name IN (
              'Зенит',
              'Спартак Москва',
              'ЦСКА Москва',
              'Динамо Москва',
              'Анжи',
              'Рубин',
              'Локомотив Москва',
              'Кубань'
          )
      )
      OR m.g_team_id IN (
          SELECT t.id
          FROM football."Team" t
          WHERE t.name IN (
              'Зенит',
              'Спартак Москва',
              'ЦСКА Москва',
              'Динамо Москва',
              'Анжи',
              'Рубин',
              'Локомотив Москва',
              'Кубань'
          )
      )
  );

UPDATE football."Match"
SET "group" = 'B'
WHERE league_id = 1
  AND tournament_id = 31
  AND stage_id = 45
  AND "group" IS NULL;
