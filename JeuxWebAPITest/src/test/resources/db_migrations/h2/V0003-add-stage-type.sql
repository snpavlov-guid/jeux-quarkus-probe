ALTER TABLE football."Stage"
    ADD COLUMN stage_type VARCHAR(16);

UPDATE football."Stage"
SET stage_type = CASE
    WHEN name IN ('Понижение/повышение - финал', 'Золотой матч') THEN 'EXTRAPLAY'
    ELSE 'REGULAR'
END;

ALTER TABLE football."Stage"
    ALTER COLUMN stage_type VARCHAR(16) NOT NULL;
