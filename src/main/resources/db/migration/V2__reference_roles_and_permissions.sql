-- Reference data: permissions and roles.
-- No users and no credentials are seeded here. The first administrator is created at
-- startup only when APP_BOOTSTRAP_ENABLED=true and APP_BOOTSTRAP_ADMIN_* are provided.

INSERT INTO app_permission (code, description) VALUES
    ('news:read',     'Read news articles and categories in the admin console'),
    ('news:write',    'Create, edit and archive news articles and categories'),
    ('news:publish',  'Publish and unpublish news articles'),
    ('users:read',    'View staff user accounts'),
    ('users:manage',  'Create, update, disable staff users and assign roles'),
    ('roles:read',    'View roles and permissions'),
    ('audit:read',    'View the audit trail');

INSERT INTO app_role (id, code, name, description, system_role) VALUES
    ('a1000000-0000-4000-8000-000000000001', 'SUPER_ADMIN', 'Super administrator',
     'Full access, including user and role management. Cannot be deleted.', true),
    ('a1000000-0000-4000-8000-000000000002', 'EDITOR', 'Editor',
     'Can write and publish news content.', true),
    ('a1000000-0000-4000-8000-000000000003', 'REPORTER', 'Reporter',
     'Can write drafts but cannot publish.', true),
    ('a1000000-0000-4000-8000-000000000004', 'AUDITOR', 'Auditor',
     'Read-only access to content, users, roles and the audit trail.', true);

-- SUPER_ADMIN receives every permission currently defined.
INSERT INTO app_role_permission (role_id, permission_code)
SELECT 'a1000000-0000-4000-8000-000000000001', code FROM app_permission;

INSERT INTO app_role_permission (role_id, permission_code) VALUES
    ('a1000000-0000-4000-8000-000000000002', 'news:read'),
    ('a1000000-0000-4000-8000-000000000002', 'news:write'),
    ('a1000000-0000-4000-8000-000000000002', 'news:publish'),
    ('a1000000-0000-4000-8000-000000000003', 'news:read'),
    ('a1000000-0000-4000-8000-000000000003', 'news:write'),
    ('a1000000-0000-4000-8000-000000000004', 'news:read'),
    ('a1000000-0000-4000-8000-000000000004', 'users:read'),
    ('a1000000-0000-4000-8000-000000000004', 'roles:read'),
    ('a1000000-0000-4000-8000-000000000004', 'audit:read');
