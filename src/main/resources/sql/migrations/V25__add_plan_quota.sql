ALTER TABLE plans ADD COLUMN IF NOT EXISTS ai_quota INTEGER NOT NULL DEFAULT 5;

UPDATE plans SET ai_quota = 5 WHERE subscription_type = 'FREE';
UPDATE plans SET ai_quota = 100 WHERE subscription_type = 'PREMIUM';

CREATE TABLE IF NOT EXISTS plan_features (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id UUID NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
    label VARCHAR(255) NOT NULL,
    order_index INTEGER NOT NULL DEFAULT 0
);