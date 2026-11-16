# Brief: admin panel (`khatrik34/sports-web-admin-panel`)

Self-contained hand-off for the session building the admin UI against this backend. Backend `main`:
**71 integration tests green**. Everything listed under "Available now" is implemented; nothing in
the "Not built yet" section exists, so do not invent it.

## How to run the backend locally

```bash
cp .env.example .env      # DB_PASSWORD + APP_JWT_SECRET (openssl rand -base64 48)
docker compose up -d db   # PostgreSQL 16 on 5432
mvn spring-boot:run       # http://localhost:8080, Flyway migrates automatically
```

First login: `APP_BOOTSTRAP_ENABLED=true` with `APP_BOOTSTRAP_ADMIN_EMAIL` /
`APP_BOOTSTRAP_ADMIN_PASSWORD` for one start, then set it back to `false`.
Allow the panel origin: `APP_CORS_ALLOWED_ORIGINS=http://localhost:3001`.

## Auth contract

| Call | Body | Response |
| --- | --- | --- |
| `POST /api/v1/auth/login` | `{email, password}` | `TokenResponse` |
| `POST /api/v1/auth/refresh` | `{refreshToken}` | new `TokenResponse` (old token revoked) |
| `POST /api/v1/auth/logout` | `{refreshToken}` | `204` |
| `POST /api/v1/auth/password` | `{currentPassword, newPassword}` | `204` — clears `mustChangePassword`, revokes every session |
| `GET /api/v1/auth/me` | — (Bearer) | `AuthenticatedUserResponse` |

`TokenResponse`: `accessToken`, `tokenType` (`Bearer`), `accessTokenExpiresAt`, `refreshToken`,
`refreshTokenExpiresAt`, `user`. Access tokens last 15 minutes; refresh tokens rotate and are revoked
on reuse. Keep both tokens **server-side** (HttpOnly cookies written by Next.js route handlers) and
proxy through same-origin route handlers; the browser never talks to the API host.

`AuthenticatedUserResponse`: `id`, `email`, `displayName`, `mustChangePassword`, `roles[]`,
`permissions[]`. Menu and actions are driven by `permissions`; the backend enforces them anyway.

## Errors

RFC 9457 `application/problem+json` with a stable `code`:

```json
{ "type": "about:blank", "title": "Conflict", "status": 409, "code": "slug_taken",
  "detail": "Another entry already uses this slug.", "errors": [] }
```

Branch on `code`: `not_found`, `invalid_slug`, `slug_taken`, `invalid_reference`,
`invalid_transition`, `not_publishable`, `duplicate_team`, `duplicate_section`, `invalid_filter`,
`invalid_date`, `invalid_status`, `invalid_path`, `redirect_exists`, `invalid_config`, `invalid_url`,
`weak_password`, `current_password_incorrect`, `password_reused`, `too_many_attempts`,
`password_change_required`, `forbidden`, `unauthorized`, `validation_failed`.

## Paging

`{content, page, size, totalElements, totalPages, first, last}`; `page` is 0-based, `size` ≤ 100.

## Available now (v1)

| Area | Endpoints | Permission |
| --- | --- | --- |
| News | `GET/POST /admin/news`, `GET/PUT /admin/news/{id}`, `POST /{id}/publish`, `/{id}/unpublish`, `/{id}/archive` | `news:read` / `news:write` / `news:publish` |
| News categories | `GET/POST /admin/news-categories`, `PUT /{id}` | `news:read` / `news:write` |
| Teams | `GET/POST /admin/teams`, `GET/PUT /{id}`, `POST /{id}/activate`, `/{id}/deactivate`, `DELETE /{id}` | `cricket:read` / `cricket:write` |
| Players | `GET/POST /admin/players`, `GET/PUT /{id}`, `POST /{id}/activate`, `/{id}/deactivate` | `cricket:*` |
| Series | `GET/POST /admin/tournaments`, `GET/PUT /{id}`, `POST /{id}/activate`, `/{id}/deactivate`, `GET/PUT /{id}/standings` | `cricket:*` |
| Fixtures | `GET/POST /admin/matches`, `GET/PUT /{id}`, `POST /{id}/show`, `/{id}/hide`, `DELETE /{id}` | `cricket:*` |
| Scorecard | `GET/PUT /admin/matches/{id}/scorecard` (PUT replaces the whole innings set) | `cricket:*` |
| Commentary | `GET/POST /admin/matches/{id}/commentary`, `DELETE /{id}/commentary/{entryId}` | `cricket:*` |
| Videos | `GET/POST /admin/videos`, `GET/PUT /{id}`, `POST /{id}/show`, `/{id}/hide`, `DELETE /{id}` | `videos:read` / `videos:write` |
| Redirects | `GET/POST /admin/redirects`, `GET/PUT /{id}`, `DELETE /{id}` | `seo:read` / `seo:write` |
| Homepage | `GET /admin/homepage`, `PUT /admin/homepage` (whole configuration) | `homepage:read` / `homepage:write` |
| Media | `GET/POST /admin/media`, `GET/PUT /{id}`, `DELETE /{id}` — URL registration only | `media:read` / `media:write` |
| Staff users | `GET /admin/users`, `GET /{id}`, `POST`, `PATCH /{id}`, `PUT /{id}/roles`, `POST /{id}/password`, `POST /{id}/unlock` | `users:read` / `users:manage` |
| Roles | `GET /admin/roles`, `GET /admin/roles/permissions` | `roles:read` |
| Audit | `GET /admin/audit-logs?page&size&action&entityType&entityId&from&to` | `audit:read` |
| Docs | `GET /v3/api-docs`, `GET /swagger-ui/index.html`; CI publishes `openapi.json` as the build artifact **openapi-snapshot** | — |

Scorecard request body: `{ innings: [{ inningsNumber, battingTeamId, runs, wickets, overs,
declared, batting: [{ position, batter, dismissal, runs, balls, fours, sixes, notOut }],
bowling: [{ position, bowler, overs, maidens, runsConceded, wickets }] }] }`.

Homepage body: `{ sections: [{ key, type, title, sortOrder, enabled, config }] }` where `type` is
`HERO | HEADLINE_LIST | MATCH_STRIP | POINTS_TABLE | VIDEO_RAIL | CUSTOM` and `config` is JSON
(e.g. `{"articleSlugs":["a","b"],"limit":3}` or `{"tournamentSlug":"wtc-2026"}`).

## Forced password change (now enforced by the API)

If the account has `mustChangePassword`, the access token carries it and **every business endpoint
returns `403` with `code: "password_change_required"`**; only `/api/v1/auth/me`, `/api/v1/auth/password`,
`/api/v1/auth/refresh` and `/api/v1/auth/logout` stay reachable. So route to the change-password screen
right after login when `user.mustChangePassword` is true, and treat that 403 anywhere else the same way.
A successful change revokes every session — send the user back to `/login`.

`POST /auth/login` can also answer `429` with `code: "too_many_attempts"` (per-IP limit).

## Not built yet (do not fake in the UI)

News revisions/history · binary media uploads (only URL registration) · image resizing/variants ·
2FA · JWT key rotation. Per-IP sign-in limiting **is** implemented (`429 too_many_attempts`).

## Screens

Login · Change password · Dashboard · News list + editor (draft/schedule/publish/unpublish/archive,
SEO fields, cover image URL + alt, category) · News categories · Teams · Players · Series +
points-table editor · Fixtures (scores, state, show/hide) · Scorecard editor · Commentary form ·
Videos · Redirects · Homepage block builder · Media library · Staff users & roles · Audit log.

## Non-negotiables

1. Backend enforces authorization; the UI hides what the user cannot do and still handles `403`.
2. No secrets, no hardcoded hosts in browser code, `.env.example` only.
3. Real errors surfaced; no silent mock fallback; no fake success toasts.
4. `npm run typecheck`, `npm run lint`, `npm run build` must pass before claiming anything works.
5. Maintain `docs/PROGRESS.md` in that repo with verified / not-verified items.
6. If an endpoint or field is missing, list it as a backend dependency instead of inventing it.
