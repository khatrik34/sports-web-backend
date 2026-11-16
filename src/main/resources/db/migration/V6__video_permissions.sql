-- Phase 3 permissions for videos.

INSERT INTO app_permission (code, description) VALUES
    ('videos:read',  'Read video entries in the admin console'),
    ('videos:write', 'Create, edit, publish and delete video entries');

INSERT INTO app_role_permission (role_id, permission_code) VALUES
    ('a1000000-0000-4000-8000-000000000001', 'videos:read'),
    ('a1000000-0000-4000-8000-000000000001', 'videos:write'),
    ('a1000000-0000-4000-8000-000000000002', 'videos:read'),
    ('a1000000-0000-4000-8000-000000000002', 'videos:write'),
    ('a1000000-0000-4000-8000-000000000003', 'videos:read'),
    ('a1000000-0000-4000-8000-000000000004', 'videos:read');
