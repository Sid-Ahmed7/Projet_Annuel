ALTER TABLE challenge_qcm_options ADD COLUMN IF NOT EXISTS option_position INTEGER DEFAULT 0;

WITH numbered AS (
    SELECT ctid, ROW_NUMBER() OVER (PARTITION BY question_id ORDER BY ctid) - 1 AS rn
    FROM challenge_qcm_options
)
UPDATE challenge_qcm_options SET option_position = numbered.rn
FROM numbered WHERE challenge_qcm_options.ctid = numbered.ctid;
