# Brief: admin panel (`khatrik34/sports-web-admin-panel`)

Copy-paste brief for the session that builds the admin UI. The backend it consumes is this
repository, already merged to `main` and green in CI.

## Reality check

- `sports-web-admin-panel` currently contains **only a README** — nothing to preserve, no
  framework chosen, no API client.
- The backend enforces every permission. The UI only reflects them; it is never the security layer.
- Anything the UI needs that is not listed under "Available now" does not exist yet. Do not invent
  endpoints or fields — list them as backend dependencies.

## How to run the backend locally

```bash
cp .env.example .env      # fill DB_PASSWORD and APP_JWT_SECRET (openssl rand -base64 48)
docker compose up -d db   # PostgreSQL 16 on 5432
mvn spring-boot:run       # http://localhost:8080, Flyway migrates automatically
```

First login: set `APP_BOOTSTRAP_ENABLED=true` plus `APP_BOOTSTRAP_ADMIN_EMAIL` /
`APP_BOOTSTRAP_ADMIN_PASSWORD` for one start, then switch it back to `false`.
Allow the panel's origin: `APP_CORS_ALLOWED_ORIGINS=http://localhost:3001`.

## Auth contract

| Call | Body | Response |
| --- | --- | --- |
| `POST /api/v1/auth/login` | `{email, password}` | `TokenResponse` |
| `POST /api/v1/auth/refresh` | `{refreshToken}` | new `TokenResponse` (old refresh token is revoked) |
| `POST /api/v1/auth/logout` | `{refreshToken}` | `204` |
| `GET /api/v1/auth/me` | — (Bearer) | `AuthenticatedUserResponse` |

`TokenResponse`: `accessToken`, `tokenType` (`Bearer`), `accessTokenExpiresAt`, `refreshToken`,
`refreshTokenExpiresAt`, `user`.
`AuthenticatedUserResponse`: `id`, `email`, `displayName`, `mustChangePassword`, `roles[]`,
`permissions[]`.

Access token lifetime is 15 minutes; refresh tokens rotate and are revoked on reuse. Keep both
tokens **server-side only** (HttpOnly cookies written by Next.js route handlers). Never put them in
`localStorage`, never call the backend host from the browser — proxy through same-origin route
handlers.

## Errors

RFC 9457 `application/problem+json`:

```json
{ "type": "about:blank", "title": "Conflict", "status": 409, "code": "slug_taken",
  "detail": "Another entry already uses this slug.", "errors": [] }
```

Branch on `code`: `not_found`, `invalid_slug`, `slug_taken`, `invalid_reference`,
`invalid_transition`, `not_publishable`, `duplicate_team`, `invalid_filter`, `invalid_date`,
`forbidden`, `unauthorized`, `validation_failed`.

## Paging

`{content, page, size, totalElements, totalPages, first, last}`; `page` is 0-based, `size` ≤ 100.

## Available now (v1)

| Area | Endpoints | Permission |
| --- | --- | --- |
| News | `GET/POST /admin/news`, `GET/PUT /admin/news/{id}`, `POST /{id}/publish`, `/{id}/unpublish`, `/{id}/archive` | `news:read`, `news:write`, `news:publish` |
| News categories | `GET/POST /admin/news-categories`, `PUT /{id}` | `news:read` / `news:write` |
| Teams | `GET/POST /admin/teams`, `GET/PUT /{id}`, `POST /{id}/activate`, `/{id}/deactivate`, `DELETE /{id}` | `cricket:read` / `cricket:write` |
| Players | `GET/POST /admin/players`, `GET/PUT /{id}`, `POST /{id}/activate`, `/{id}/deactivate` | `cricket:*` |
| Series | `GET/POST /admin/tournaments`, `GET/PUT /{id}`, `POST /{id}/activate`, `/{id}/deactivate`, `GET/PUT /{id}/standings` | `cricket:*` |
| Fixtures | `GET/POST /admin/matches`, `GET/PUT /{id}`, `POST /{id}/show`, `/{id}/hide`, `DELETE /{id}` | `cricket:*` |
| Staff users | `GET /admin/users`, `GET /{id}`, `POST`, `PATCH /{id}`, `PUT /{id}/roles`, `POST /{id}/password`, `POST /{id}/unlock` | `users:read` / `users:manage` |
| Roles | `GET /admin/roles`, `GET /admin/roles/permissions` | `roles:read` |
| Audit | `GET /admin/audit-logs?page&size&action&entityType&entityId&from&to` | `audit:read` |
| Docs | `GET /v3/api-docs`, `GET /swagger-ui/index.html` | public when `APP_DOCS_PUBLIC=true` |

## Not built yet (backend Phase 3 — do not fake in the UI)

Scorecards (innings/batting/bowling), ball-by-ball commentary, videos, media library/uploads,
redirects/SEO tooling, change-password endpoint, `/rankings` data, homepage configuration blocks,
news revisions. Show these screens as "coming soon" or omit them; do not mock them into production
paths.

## Screens

Login · Dashboard · News list + editor (draft/schedule/publish/unpublish/archive, SEO fields,
cover image URL + alt, category) · News categories · Teams · Players · Series + points-table editor
(full replacement) · Fixtures (scores, state, show/hide) · Staff users & roles · Audit log.

## Non-negotiables

1. Backend enforces authorization; the UI hides what the user cannot do and still handles `403`.
2. No secrets, no hardcoded hosts in browser code, `.env.example` only.
3. Real errors are surfaced; no silent mock fallback.
4. `mustChangePassword` forces the change-password screen (endpoint arrives in Phase 3; until then
   show a warning).
5. `npm run typecheck`, `npm run lint`, `npm run build` must pass before claiming anything is done.
6. Maintain `docs/PROGRESS.md` in that repo with verified / not-verified items.
