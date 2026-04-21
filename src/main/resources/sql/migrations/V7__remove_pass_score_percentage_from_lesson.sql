DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='lesson' AND column_name='pass_score_percentage') THEN
        ALTER TABLE lesson DROP COLUMN pass_score_percentage;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='topic' AND column_name='order_index') THEN
        ALTER TABLE topic DROP COLUMN order_index;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='languages' AND column_name='order_index') THEN
        ALTER TABLE languages DROP COLUMN order_index;
    END IF;
END $$;
