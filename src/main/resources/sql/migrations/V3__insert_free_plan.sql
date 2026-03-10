-- Il faudrait qu'on regarde la description à mettre pour le plan gratuit c'est surtout pour la descritpion après 
-- le reste on ne modifie pas à part si d'autres éléments sont intéressants à ajouter pour le plan gratuit
INSERT INTO plans (id, name, description, price, currency, payment_interval, subscription_type, stripe_price_id, is_active, created_date, updated_date)
SELECT
    gen_random_uuid(),
    'Free',
    'This is the plan free',
    0.00,
    'EUR',
    NULL,
    'FREE',
    NULL,
    true,
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM plans WHERE subscription_type = 'FREE' AND is_active = true
);