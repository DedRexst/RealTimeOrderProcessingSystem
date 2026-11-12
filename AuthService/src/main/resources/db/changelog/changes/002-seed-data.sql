CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (created_time, updated_time, first_name, last_name, email, phone, password, state, deleted)
SELECT now(), now(), v.first_name, v.last_name, v.email, v.phone, '{bcrypt}' || crypt(v.raw_password, gen_salt('bf', 10)), v.state, v.deleted
FROM (VALUES
    ('Admin', 'Root',       'admin@example.com', '+380000000000', 'Admin123!',  'ACTIVE',   FALSE),
    ('Ivan',  'Petrenko',   'ivan@example.com',  '+380501112233', 'Password1!', 'ACTIVE',   FALSE),
    ('Olena', 'Shevchenko', 'olena@example.com', '+380672223344', 'Password1!', 'ACTIVE',   FALSE),
    ('Taras', 'Bondar',     'taras@example.com', '+380933334455', 'Password1!', 'INACTIVE', FALSE),
    ('Maria', 'Koval',      'maria@example.com', '+380634445566', 'Password1!', 'ACTIVE',   TRUE)
) AS v (first_name, last_name, email, phone, raw_password, state, deleted)
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE lower(u.email) = v.email);

INSERT INTO user_roles (user_id, role)
SELECT r.user_id, r.role
FROM (
    SELECT id AS user_id, 'USER' AS role FROM users
        WHERE email IN ('admin@example.com', 'ivan@example.com', 'olena@example.com', 'taras@example.com', 'maria@example.com')
    UNION ALL
    SELECT id, 'ADMIN' FROM users WHERE email = 'admin@example.com'
) AS r
WHERE NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = r.user_id AND ur.role = r.role);

INSERT INTO refresh_tokens (created_time, updated_time, token_hash, user_id, expires_at, revoked, revoked_at)
SELECT now(), now(), encode(digest(t.raw_token, 'sha256'), 'hex'), u.id, t.expires_at, t.revoked, t.revoked_at
FROM (VALUES
    ('admin@example.com', 'seed-refresh-admin',         now() + INTERVAL '30 days', FALSE, NULL::TIMESTAMPTZ),
    ('ivan@example.com',  'seed-refresh-ivan',          now() + INTERVAL '30 days', FALSE, NULL),
    ('ivan@example.com',  'seed-refresh-ivan-old',      now() - INTERVAL '1 day',   FALSE, NULL),
    ('olena@example.com', 'seed-refresh-olena-revoked', now() + INTERVAL '20 days', TRUE,  now() - INTERVAL '2 hours')
) AS t (email, raw_token, expires_at, revoked, revoked_at)
JOIN users u ON u.email = t.email
WHERE NOT EXISTS (
    SELECT 1 FROM refresh_tokens rt WHERE rt.token_hash = encode(digest(t.raw_token, 'sha256'), 'hex')
);

INSERT INTO oauth_clients (created_time, updated_time, client_id, name, description, contact_email, secret_hash, enabled, secret_rotated_at)
SELECT now(), now(), v.client_id, v.name, v.description, v.contact_email, '{bcrypt}' || crypt(v.client_id || '-secret', gen_salt('bf', 10)), v.enabled, v.secret_rotated_at
FROM (VALUES
    ('order-service',     'OrderService',     'Internal order service',           'orders-team@example.com',    TRUE,  NULL::TIMESTAMPTZ),
    ('inventory-service', 'InventoryService', 'Internal inventory service',       'inventory-team@example.com', TRUE,  NULL),
    ('analytics-service', 'AnalyticsService', 'Internal analytics service',       'analytics-team@example.com', TRUE,  now() - INTERVAL '10 days'),
    ('legacy-partner',    'Legacy Partner',   'Disabled third-party integration', 'partner@example.com',        FALSE, NULL)
) AS v (client_id, name, description, contact_email, enabled, secret_rotated_at)
WHERE NOT EXISTS (SELECT 1 FROM oauth_clients c WHERE c.client_id = v.client_id);

INSERT INTO oauth_client_scopes (client_id, scope)
SELECT c.id, s.scope
FROM (VALUES
    ('order-service',     'tokens:introspect'),
    ('order-service',     'orders:write'),
    ('order-service',     'inventory:read'),
    ('inventory-service', 'tokens:introspect'),
    ('inventory-service', 'inventory:write'),
    ('analytics-service', 'tokens:introspect'),
    ('analytics-service', 'orders:read'),
    ('analytics-service', 'inventory:read'),
    ('legacy-partner',    'orders:read')
) AS s (client_id, scope)
JOIN oauth_clients c ON c.client_id = s.client_id
WHERE NOT EXISTS (SELECT 1 FROM oauth_client_scopes cs WHERE cs.client_id = c.id AND cs.scope = s.scope);
--rollback DELETE FROM oauth_clients WHERE client_id IN ('order-service', 'inventory-service', 'analytics-service', 'legacy-partner');
