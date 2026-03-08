
INSERT INTO accounts (
    id,
    email,
    password,
    first_name,
    last_name,
    username,
    role,
    status,
    auth_key,
    failed_login_attempts,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    '${email}',                    
    '${password}',           
    '${first_name}',               
    '${last_name}',                
    'admin',                             
    'ADMIN',                             
    'ACTIVE',                          
    '${secret_key}',          
    0,                                  
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;       

