-- Phase 2 permissions. SUPER_ADMIN receives them explicitly because V2 granted it the
-- permissions that existed at that time.

INSERT INTO app_permission (code, description) VALUES
    ('cricket:read',  'Read teams, players, series and fixtures in the admin console'),
    ('cricket:write', 'Create and edit teams, players, series, fixtures and points tables');

INSERT INTO app_role_permission (role_id, permission_code) VALUES
    -- SUPER_ADMIN
    ('a1000000-0000-4000-8000-000000000001', 'cricket:read'),
    ('a1000000-0000-4000-8000-000000000001', 'cricket:write'),
    -- EDITOR
    ('a1000000-0000-4000-8000-000000000002', 'cricket:read'),
    ('a1000000-0000-4000-8000-000000000002', 'cricket:write'),
    -- REPORTER: read only, may write news but not fixtures
    ('a1000000-0000-4000-8000-000000000003', 'cricket:read'),
    -- AUDITOR
    ('a1000000-0000-4000-8000-000000000004', 'cricket:read');
