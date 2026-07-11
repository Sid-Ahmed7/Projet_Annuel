ALTER TABLE challenge_sorting_items ADD COLUMN IF NOT EXISTS item_position INTEGER DEFAULT 0;
ALTER TABLE challenge_sorting_order ADD COLUMN IF NOT EXISTS order_position INTEGER DEFAULT 0;

WITH numbered AS (
    SELECT ctid, ROW_NUMBER() OVER (PARTITION BY exercise_id ORDER BY ctid) - 1 AS rn
    FROM challenge_sorting_items
)
UPDATE challenge_sorting_items SET item_position = numbered.rn
FROM numbered WHERE challenge_sorting_items.ctid = numbered.ctid;

WITH numbered AS (
    SELECT ctid, ROW_NUMBER() OVER (PARTITION BY exercise_id ORDER BY ctid) - 1 AS rn
    FROM challenge_sorting_order
)
UPDATE challenge_sorting_order SET order_position = numbered.rn
FROM numbered WHERE challenge_sorting_order.ctid = numbered.ctid;
