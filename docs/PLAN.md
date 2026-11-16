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
- Audience split:
  - `/api/v1/public/**` — anonymous, GET only, **published content only**.
  - `/api/v1/auth/**` — login/refresh/logout/me.
  - `/api/v1/admin/**` — JWT required **plus** a permission authority per endpoint.
- JSON is camelCase, timestamps are ISO-8601 UTC (`2026-11-16T09:30:00Z`).
- Collections are paged: `{ content: [...], page, size, totalElements, totalPages, first, last }`.
  `page` is 0-based, `size` caps at 100.
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

## Phase 2 — Media + SEO completion

- Media library: upload to S3-compatible storage (presigned), image variants, alt text,
  attribution; `MEDIA_MANAGE` permission; news `cover_image` becomes a media reference.
- URL migration kit: `redirect` table (301/302/410), import of legacy WordPress/Next.js
  paths, `sitemap.xml` + `robots.txt` generated from stored content.
- News revisions (draft autosave history), scheduled publishing job, tags, related articles.
- OpenAPI contract file checked into `docs/openapi/` for frontend type generation.

**Blocked on:** frontend URL inventory + admin panel contracts (see `docs/AUDIT.md` §4).

## Phase 3 — Sports data (admin-entered, no live feeds)

- Teams (name, slug, crest media, country), players (profile, position, team history),
  tournaments (format, season, dates), fixtures (home/away, venue, kickoff, status),
  match scores (per-inning for cricket where relevant), ball-by-ball commentary entries,
  points tables (tournament standings, computed from results or entered).
- Public read API mirrors the news pattern (published/final only, no admin fields);
  editorial workflow for fixtures (draft → published, provisional vs final scores).
- **Explicit non-goal:** no scraping, polling or websocket integration with any external
  cricket data provider, no claim of real-time scores. Only what admins enter (or a future
  explicitly-approved provider adapter) is served.

## Phase 4 — Homepage, videos, operations

- Homepage configuration (ordered blocks: hero, featured news, match strip, points table,
  video rail) as versioned JSON with validation and preview, served at
  `/api/v1/public/homepage`.
- Videos (YouTube/external embeds + metadata, no hosting), video collections.
- Hardening: per-IP login rate limiting, JWT key rotation (`kid`), optional 2FA for
  SUPER_ADMIN, audit retention/exports, structured logs + metrics, staging/prod profiles.

## Assumptions to confirm (all configurable, all reversible)

| id | Assumption | Where |
| --- | --- | --- |
| A1 | Article URL shape is `/{categorySlug}/{slug}` (fallback `/{slug}` when no category) | `app.public-site.article-path-template` |
| A2 | Public site origin is configured via `APP_PUBLIC_SITE_BASE_URL` and used for absolute `url`/canonical in public payloads and the sitemap feed | `app.public-site.base-url` |
| A3 | The admin panel can send `Authorization: Bearer` and store tokens itself | auth contract |
| A4 | Body content is HTML produced by an editor; a `body_format` column exists so Markdown can be added without migration | `news_article.body_format` |
| A5 | Category slugs are editorial and stable; category rename does not rewrite article paths | `news_category` |
| A6 | No pre-existing users exist to migrate; the first admin comes from `APP_BOOTSTRAP_*` | `AdminBootstrapRunner` |

## Definition of done (per phase)

1. Migrations apply from an empty database and Hibernate `validate` passes.
2. Integration tests on real PostgreSQL cover happy path + the security-negative cases
   (draft leakage, missing permission, expired/revoked token).
3. No secret, password or environment-specific URL is committed.
4. `docs/PROGRESS.md` updated with what is verified, what is not, and why.
5. OpenAPI reflects the endpoints; breaking changes require `/api/v2`, never an in-place edit.
