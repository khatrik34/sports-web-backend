-- Phase 3 permissions: redirects, homepage configuration and media metadata.

INSERT INTO app_permission (code, description) VALUES
    ('seo:read',        'Read SEO redirects'),
    ('seo:write',       'Create, edit and delete SEO redirects'),
    ('homepage:read',   'Read the homepage configuration'),
    ('homepage:write',  'Replace the homepage configuration'),
    ('media:read',      'Read registered media assets'),
    ('media:write',     'Register, update and remove media assets');

INSERT INTO app_role_permission (role_id, permission_code) VALUES
    ('a1000000-0000-4000-8000-000000000001', 'seo:read'),
    ('a1000000-0000-4000-8000-000000000001', 'seo:write'),
    ('a1000000-0000-4000-8000-000000000001', 'homepage:read'),
    ('a1000000-0000-4000-8000-000000000001', 'homepage:write'),
    ('a1000000-0000-4000-8000-000000000001', 'media:read'),
    ('a1000000-0000-4000-8000-000000000001', 'media:write'),
    ('a1000000-0000-4000-8000-000000000002', 'seo:read'),
    ('a1000000-0000-4000-8000-000000000002', 'seo:write'),
    ('a1000000-0000-4000-8000-000000000002', 'homepage:read'),
    ('a1000000-0000-4000-8000-000000000002', 'homepage:write'),
    ('a1000000-0000-4000-8000-000000000002', 'media:read'),
    ('a1000000-0000-4000-8000-000000000002', 'media:write'),
    ('a1000000-0000-4000-8000-000000000003', 'media:read'),
    ('a1000000-0000-4000-8000-000000000004', 'seo:read'),
    ('a1000000-0000-4000-8000-000000000004', 'homepage:read'),
    ('a1000000-0000-4000-8000-000000000004', 'media:read');
