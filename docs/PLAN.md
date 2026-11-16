# Implementation plan

Status legend: **DONE** = written here (static-validated) · **CI-VERIFIED** = compiles and
passes tests in GitHub Actions · **OPEN** = not implemented.

See `docs/PROGRESS.md` for the live status of every item and `docs/AUDIT.md` for why some
facts are still unknown.

## Architecture

Single Spring Boot modular monolith (`com.sportsweb.backend`), one deployable, one
PostgreSQL database, package-per-module with no cross-module entity leakage outside
service interfaces:

```
com.sportsweb.backend
├── common        error handling, paging, audit trail, small utilities
├── config        OpenAPI, public-site/SEO properties
├── security      JWT issuing/validation, auth endpoints, users, roles, permissions
└── news          news articles, categories, public read API
```

Planned later modules (teams, players, tournaments, fixtures, scores/commentary, points
tables, videos, media, homepage config) plug in as sibling packages, each owning its
tables, Flyway migrations (`V<n>__<module>_*`) and its own `admin` + `public` controllers.

### API conventions (fixed now, so clients can be built against them)

- Base path: `/api/v{version}`; Phase 1 serves `v1` only. Version is in the path, never in
  a header, so a v2 can coexist.
- Public URLs follow the shipped website exactly (`docs/CONTRACTS.md` §1): articles are
  `/news/{slug}`, categories are `/news?filter=<value>`, sitemaps come from the API but the
  static section paths stay owned by the site.
- Audience split:
  - `/api/v1/public/**` — anonymous, GET only, **published content only**.
  - `/api/v1/auth/**` — login/refresh/logout/me.
  - `/api/v1/admin/**` — JWT required **plus** a permission authority per endpoint.
- JSON is camelCase, timestamps are ISO-8601 UTC (`2026-11-16T09:30:00Z`).
- Collections are paged: `{ content: [...], page, size, totalElements, totalPages, first, last }`.
  `page` is 0-based, `size` caps at 100. Payloads are returned bare (no
  `{success,message,data}` envelope); errors are RFC 9457. Rationale and the reversal cost are
  in `docs/CONTRACTS.md` §4 (decision D1).
- Errors are RFC 9457 `application/problem+json` with `type`, `title`, `status`, `detail`,
  `instance`, and `errors[]` for field validation failures. No stack traces, no SQL,
  no internal identifiers in messages.
- Auth: `Authorization: Bearer <accessToken>`; access tokens 15 min, refresh tokens 14 days,
  rotating, hashed at rest, revocable. No cookies, so no CSRF surface.

### Security model

- Authorities on the token: `ROLE_<ROLE_CODE>` (coarse) plus permission codes
  `news:read`, `news:write`, `news:publish`, `users:read`, `users:manage`, `roles:read`,
  `audit:read`. Endpoints are guarded by permission, not by role, so roles stay data.
- Bootstrap roles: `SUPER_ADMIN` (all), `EDITOR` (news write/publish), `REPORTER` (news
  write, no publish), `AUDITOR` (read-only: news/users/roles/audit).
- Passwords: BCrypt (cost 12), policy ≥ 12 chars with 3 of 4 character classes.
  Five failed logins lock the account for 15 minutes.
- Access tokens are stateless and short-lived; disabling a user or changing roles revokes
  refresh tokens immediately and takes effect on the next access-token refresh (≤ 15 min).
  This trade-off is intentional and recorded in `docs/PROGRESS.md`.
- Audit trail: append-only `audit_log`, actor + IP + user-agent + before/after snapshot.
  Business events share the business transaction; security events (failed login, refresh
  token reuse) are written in a separate transaction so they survive the failure.

## Phase 1 — Foundation + security + news (this change)

| # | Deliverable | Acceptance criteria |
| --- | --- | --- |
| 1.1 | Maven project, Java 21, Spring Boot 3.3, `.gitignore`, CI workflow | `mvn -B verify` green in CI |
| 1.2 | Flyway baseline schema + seeded roles/permissions (no users) | migrations apply to PostgreSQL 16; Hibernate `validate` passes |
| 1.3 | JWT auth: login, refresh with rotation, logout, `me` | tests: login ok/bad password/locked; refresh rotates and revokes the old token; reuse revokes the family |
| 1.4 | RBAC: 7 permissions, 4 roles, method-level enforcement | tests: REPORTER gets 403 on publish, anonymous gets 401 on admin |
| 1.5 | Users/roles admin API | create/update/disable/unlock/assign roles/password reset; last-SUPER_ADMIN and self-disable guards |
| 1.6 | Audit trail + admin audit query | every mutation and auth event produces exactly one row with actor/entity snapshot |
| 1.7 | News lifecycle: draft → publish (or scheduled) → unpublish → archive | illegal transitions rejected; publishing requires title/summary/body |
| 1.8 | Public API: news list/detail, categories, sitemap feed | drafts, archived and future-dated articles are absent and 404; no admin fields in payloads |
| 1.9 | OpenAPI 3 + Swagger UI with bearer scheme | `/v3/api-docs` documents all endpoints |
| 1.10 | Docs: audit, plan, progress, README, `.env.example`, docker-compose | a new dev can start Postgres + app from the README alone |

## Phase 2 — Cricket domain: teams, players, series, fixtures, points — SHIPPED (CI-VERIFIED)

Delivered on this branch (`docs/PROGRESS.md` §Phase 2 has the item-by-item evidence):

| # | Deliverable | Acceptance criteria | State |
| --- | --- | --- | --- |
| 2.1 | Teams + players: schema, admin CRUD, soft deactivation | deactivated rows never appear publicly; slug collisions are 409 on update, auto-suffixed on create | DONE |
| 2.2 | Series: CRUD, participating teams, format/season/host/dates | public series exposes `teamSlugs` for `/cricket/series/{slug}?filter=teams` | DONE |
| 2.3 | Fixtures: CRUD, state (`SCHEDULED/LIVE/COMPLETED/ABANDONED`), scores, show/hide | public list filters exactly like the site (`?filter=live|upcoming|recent|international|domestic|league`, `?team=`, `?tournament=`, `?date=`) | DONE |
| 2.4 | Points tables: atomic replacement per series, ordered by position | duplicate teams rejected, removed rows disappear | DONE |
| 2.5 | RBAC + audit for the whole domain | `cricket:read` / `cricket:write`; REPORTOR read-only; every mutation audited | DONE |
| 2.6 | Honest data policy | scores are staff-entered; no external live-feed integration or claim | BY DESIGN |

## Phase 3 — Match detail depth, videos, media, SEO tooling — NEXT

- Scorecards: innings per fixture + batting/bowling rows (the site's `Scorecard` type), entered by staff.
- Ball-by-ball commentary entries with over/ball, kind (`DOT`, `FOUR`, `WICKET`, …) and text,
  served newest-first, paginated.
- Videos: slug, title, category, duration, thumbnail, external embed URL (no hosting), publish state.
- Media library: upload to S3-compatible storage (presigned), variants, alt text, attribution.
- Redirect/SEO kit: `redirect` table (301/302/410) for legacy paths, robots/sitemap helpers.
- Change-password endpoint so `mustChangePassword` can be enforced end-to-end.
- OpenAPI snapshot checked into `docs/openapi/` for frontend type generation.
- Rankings page (`/rankings`) data source: either computed from standings or an explicit table.

**Not blocked.** Every decision that Phase 3 needed has been taken (`docs/CONTRACTS.md` §4).

## Phase 4 — Homepage, admin panel, operations

- Homepage configuration (ordered blocks: hero, featured news, match strip, points table,
  video rail) as versioned JSON with validation and preview, served at
  `/api/v1/public/homepage`.
- Admin panel: build the UI in `sports-web-admin-panel` (currently empty) against these contracts —
  users/roles, news, cricket, media, audit viewer.
- Hardening: per-IP login rate limiting, JWT key rotation (`kid`), optional 2FA for
  SUPER_ADMIN, audit retention/exports, structured logs + metrics, staging/prod profiles.

## Integration work outside this repository

The website itself (`khatrik34/sports-web`) must swap `services/index.ts` from mocks to these APIs.
That is a change in **another repository**, so it needs its own session; the mapping and the
frontend-side fixes are specified in `docs/CONTRACTS.md` §2, §3a and §6.

## Facts and assumptions

The frontend repositories are now readable, so most earlier assumptions became facts
(`docs/AUDIT.md` §2–§4, `docs/CONTRACTS.md`).

| id | Item | State |
| --- | --- | --- |
| F1 | Article URL shape | **FACT** — `/news/{slug}` from `app/sitemap.ts`, canonical metadata and JSON-LD. Backend default updated; `app.public-site.article-path-template` still configurable. |
| F2 | Canonical/site origin | **FACT** — the site resolves metadata against `NEXT_PUBLIC_SITE_URL`; backend uses `APP_PUBLIC_SITE_BASE_URL`. Same origin must be configured in both. |
| F3 | Category URLs | **FACT** — none exist; the listing filters with `/news?filter=<value>`. No category path is emitted. |
| F4 | Sitemap + news sitemap fields | **FACT** — read from `app/sitemap.ts` and `app/news-sitemap.xml/route.ts`; served by `/public/sitemap` and `/public/news-sitemap` (`docs/CONTRACTS.md` §3). |
| F5 | Admin panel expectations | **FACT** — the repository is empty; this backend defines the contract. |
| A4 | Editor body is a single document (HTML/Markdown/plain) with `body_format` stored | ASSUMPTION (rendering policy still to be agreed) |
| A6 | No pre-existing staff users to migrate; first admin from `APP_BOOTSTRAP_*` | ASSUMPTION (confirm no production user list exists) |
| D1 | Bare payloads + RFC 9457 errors instead of a `{success,message,data,timestamp}` envelope | DECIDED (reversible at the controller layer) |
| D2 | Bearer access token + rotating refresh token, held in memory; no cookies/CSRF | DECIDED (contained change if cookies are preferred later) |
| D3 | Paging fields stay `page/size/totalElements/totalPages` | DECIDED (the UI's unused `page/pageSize/total` type is scaffolding) |

## Definition of done (per phase)

1. Migrations apply from an empty database and Hibernate `validate` passes.
2. Integration tests on real PostgreSQL cover happy path + the security-negative cases
   (draft leakage, missing permission, expired/revoked token).
3. No secret, password or environment-specific URL is committed.
4. `docs/PROGRESS.md` updated with what is verified, what is not, and why.
5. OpenAPI reflects the endpoints; breaking changes require `/api/v2`, never an in-place edit.
