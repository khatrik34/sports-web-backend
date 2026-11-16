# Progress

Last updated: 2026-11-16 · branch `arena/54d18d65-sports-web-backend`

Legend
- **CI-VERIFIED** — compiles and passes the automated suite in GitHub Actions on real PostgreSQL.
- **WRITTEN** — implemented and statically validated here, but not yet exercised by a green CI run.
- **OPEN** — not implemented.
- **BLOCKED** — cannot proceed without information/access listed at the end.

> Verification honesty: the sandbox used for this work has **no JDK, no Maven, no Docker, no
> PostgreSQL, and no access to Maven Central** (see `docs/AUDIT.md` §5). Compilation and tests
> therefore happen in CI, and the results below are quoted from actual CI runs — not assumed.

## Phase 1 — Foundation, security, news

| # | Item | Status | Evidence / notes |
| --- | --- | --- | --- |
| 1.1 | Maven project, Java 21, Spring Boot 3.3.5 | CI-VERIFIED | `mvn -B verify` green |
| 1.2 | Flyway baseline + `V2` reference roles/permissions | CI-VERIFIED | Migrations apply to PostgreSQL 16 on every CI run; Hibernate `validate` passes against them |
| 1.3 | JWT auth: login / refresh (rotating) / logout / me | CI-VERIFIED | `AuthenticationApiTest` (9 tests) |
| 1.4 | RBAC: 7 permissions, 4 roles, enforced per endpoint | CI-VERIFIED | `RbacAuthorizationTest` (6 tests) |
| 1.5 | Users/roles admin API with lockout & self-lockout guards | CI-VERIFIED | `AuthenticationApiTest`, `UserAdminService` |
| 1.6 | Append-only audit trail + admin audit query | CI-VERIFIED | `AuditTrailApiTest` (3 tests) |
| 1.7 | News lifecycle draft → publish/schedule → unpublish → archive | CI-VERIFIED | `NewsAdminApiTest` (7 tests) |
| 1.8 | Public v1 API (news, latest, categories, sitemap) with draft protection | CI-VERIFIED | `PublicNewsApiTest` (6 tests) |
| 1.9 | OpenAPI 3 + Swagger UI with bearer scheme | CI-VERIFIED | `OpenApiContractTest` asserts documented paths, `bearerAuth` scheme and Swagger UI |
| 1.10 | README, `.env.example`, docker-compose, Dockerfile, CI | CI-VERIFIED (CI) / WRITTEN (docker image not built here) | Docker build is not part of CI yet |
| 1.11 | Public URLs aligned to the shipped website (`/news/{slug}`, `/news?filter=`, `readTime`) | CI-VERIFIED | Re-audit of `sports-web@cc56fe1`; `PublicNewsApiTest` asserts canonical path and category filter URL |
| 1.12 | Sitemap + Google News sitemap feeds (published only, bounded window) | CI-VERIFIED | `PublicNewsApiTest`: `/public/sitemap`, `/public/news-sitemap?hours=` |

Suite size at last green run: **41 tests, 0 failures** (9 auth · 9 public news · 7 news admin ·
6 RBAC · 5 password policy · 3 audit · 2 OpenAPI), GitHub Actions on commit `de4aed7`.

### What CI actually proves

- The application context starts with real PostgreSQL 16, so Flyway migrations and
  Hibernate entity↔schema validation agree.
- `POST /api/v1/auth/login` issues tokens; a wrong password yields `401 invalid_credentials`
  in `application/problem+json`; five failures lock the account (`403 account_locked`).
- Refresh tokens rotate; replaying a rotated token is rejected **and revokes every session**.
  Logout revokes the presented refresh token.
- Permission enforcement: `REPORTER` can create drafts but gets `403` on publish and on user
  administration; `AUDITOR` can read the audit trail but cannot write content; anonymous
  calls to `/api/v1/admin/**` get `401`.
- Drafts, archived articles and future-dated ("scheduled") articles are absent from the
  public list and return `404` by slug; published articles expose SEO fields and none of the
  admin-only fields (`id`, `status`, `version`, ...).
- Publishing without a summary is rejected (`400 not_publishable`); duplicate slugs conflict
  (`409 slug_taken`); archived articles cannot be republished (`409 invalid_transition`).
- Public payloads use the website's real URL shape: `path`/`seo.canonicalUrl` are `/news/{slug}`,
  a category's `path` is `/news?filter=<slug>`, the sitemap lists `/news/{slug}` entries, and the
  news sitemap only contains articles published inside the requested 1–168h window.
- Failed sign-ins and publishes produce audit rows with actor, IP/user-agent and a compact
  before/after snapshot; no password material is stored.
- Password policy rejects short/weak passwords; staff creation returns no password hash.

### Not verified (do not treat as done)

- No manual smoke test of a running instance from this sandbox (cannot run the JAR here).
- Docker image build / compose stack not executed here.
- OpenAPI document is generated and its paths are asserted, but it is **not yet frozen as a
  committed contract snapshot** (planned once the frontend's expectations are known).
- No load, concurrency or penetration testing; no dependency vulnerability scan.
- The `mustChangePassword` flag is returned to clients but **does not yet block** admin
  endpoints (deliberate: blocking needs a change-password endpoint first — planned Phase 2).
- Login rate limiting is per-account lockout only (no per-IP throttling yet).

### Deliberate decisions worth reviewing

| Decision | Reason |
| --- | --- |
| Access tokens stateless, 15 min, no DB check | Throughput; role changes take effect within 15 min. Disabling a user revokes refresh tokens immediately. Accepted and documented. |
| Refresh token stored as SHA-256 hash, rotated on use, reuse ⇒ revoke all | A database leak cannot be replayed; theft is detectable. |
| Anonymous `POST /auth/logout` | Possession of the refresh token is the credential; lets an expired-access-token client end its session. Found by a failing test, fixed. |
| Two audit write paths (same-tx vs `REQUIRES_NEW`) | Business events commit with the change; security events (failed login, token reuse) survive the rollback their exception causes. |
| No user deletion, no article deletion | Preserves audit meaning and public URLs (SEO). Archive instead. |
| Permission authorities in the JWT | Endpoint checks stay server-side; the UI cannot grant itself rights. |
| `PUT /admin/news/{id}` is a full replace | Predictable; avoids "null means unchanged" ambiguity. Slug never changes implicitly. |

## Phase 2 — Cricket domain (teams, players, series, fixtures, points) — CI-VERIFIED

| id | Item | Status | Evidence / limitation |
| --- | --- | --- | --- |
| 2.1 | Schema: `cricket_team`, `cricket_player`, `cricket_tournament`, `cricket_tournament_team`, `cricket_match`, `cricket_standing` | CI-VERIFIED | Migration `V3`; entity↔column parity also checked statically before push |
| 2.2 | `cricket:read` / `cricket:write` permissions, new audit actions | CI-VERIFIED | `V4` grants SUPER_ADMIN + EDITOR write, REPORTER and AUDITOR read; `CricketApiTest` proves a REPORTER gets 403 on write |
| 2.3 | Admin teams & players: list/filter, create (slug derived), update, activate/deactivate | CI-VERIFIED | Deletes are deactivations because fixtures reference teams |
| 2.4 | Admin series: CRUD, team assignment, **atomic points-table replacement** | CI-VERIFIED | Duplicate teams in one table → `400 duplicate_team`; replacing drops removed rows |
| 2.5 | Admin fixtures: CRUD, show/hide, audited delete, score text, state | CI-VERIFIED | Hidden fixtures are invisible to the public API but still visible to admins |
| 2.6 | Public API: `/public/teams`, `/players`, `/tournaments`, `/tournaments/{slug}/standings`, `/matches`, `/matches/{slug}` | CI-VERIFIED | `?filter=live|upcoming|recent` and `?filter=international|domestic|league` both work, exactly like the site's own filter bar |
| 2.7 | OpenAPI includes the new contract | CI-VERIFIED | `OpenApiContractTest` asserts the cricket paths |
| 2.8 | Honesty constraint: no external live-score feed | BY DESIGN | Scores are typed in by staff; nothing in code or docs claims real-time integration |

Suite size at last green run: **53 tests, 0 failures** (9 auth · 12 cricket · 9 public news ·
7 news admin · 6 RBAC · 5 password policy · 3 audit · 2 OpenAPI), GitHub Actions on commit
`197387e`.

## Phase 3 — Match depth, videos, media, SEO tooling, homepage — CI-VERIFIED

| id | Item | Status | Evidence / limitation |
| --- | --- | --- | --- |
| 3.1 | Self-service change password (`POST /auth/password`) | CI-VERIFIED | Requires the current password, enforces policy, rejects reuse, revokes every refresh token, clears `mustChangePassword`. **Not enforced** as a request gate — recorded as a limitation. |
| 3.2 | Scorecards (innings + batting/bowling) with atomic replacement | CI-VERIFIED | `PUT /admin/matches/{id}/scorecard`; batting team must belong to the fixture; economy computed from overs text |
| 3.3 | Ball-by-ball commentary | CI-VERIFIED | Append + delete, newest first, paged; hidden fixtures hide their commentary |
| 3.4 | Videos | CI-VERIFIED | Admin CRUD + show/hide + delete; public only when visible **and** `publishedAt` has passed |
| 3.5 | Rankings | CI-VERIFIED | `/public/rankings` reuses the points table; picks the ongoing series for the format, else the latest |
| 3.6 | SEO redirects | CI-VERIFIED | 301/302/307/308/410, path normalisation, duplicates rejected, public resolver 404s when disabled |
| 3.7 | Homepage configuration | CI-VERIFIED | Ordered typed blocks with JSON config, replaced atomically, duplicate keys rejected, disabled blocks public-hidden |
| 3.8 | Media metadata | CI-VERIFIED (metadata only) | Register/update/delete assets by URL + alt text/caption/credit. **No binary upload or resizing** — deliberate, documented |
| 3.9 | OpenAPI snapshot | CI-VERIFIED | A test writes `target/openapi.json`; CI uploads it as the `openapi-snapshot` artifact (not committed to git) |
| 3.10 | Honesty constraint | BY DESIGN | Scores, commentary and fixtures are staff-entered; no external feed and no real-time claim |

Suite size at last green run: **71 tests, 0 failures** (12 cricket · 11 videos/match detail ·
9 auth · 9 public news · 7 news admin · 6 SEO/homepage/media · 6 RBAC · 5 password policy ·
3 audit · 3 OpenAPI).

No longer blocked by missing URL facts: the URL inventory is verified and the admin panel has no
contract of its own to preserve. Waiting only on decisions D1/D2. Work that can start immediately:
- `redirect` table + 301/410 handling and a real `sitemap.xml` renderer.
- Change-password endpoint (unblocks enforcing `mustChangePassword`).
- Optional: contract snapshot of `/v3/api-docs` as a CI check.

## Phase 4 — Search, security gates, hardening — CI-VERIFIED

| id | Item | Status | Evidence / limitation |
| --- | --- | --- | --- |
| 4.1 | Cross-section search `GET /public/search?q=&limit=` | CI-VERIFIED | One call returns news, fixtures, series, teams, players and videos; each section keeps its own visibility rules (drafts, deactivated teams, hidden fixtures/videos never surface); fewer than 2 characters → `400 query_too_short` |
| 4.2 | `mustChangePassword` enforced as a request gate | CI-VERIFIED | The flag travels in the access token; a filter refuses every business call with `403 password_change_required` until the password is changed. Only `/api/v1/auth/**` and health stay reachable |
| 4.3 | Per-IP sign-in limiting | CI-VERIFIED | In-memory sliding window (`app.security.login.ip-max-attempts`, default 30 per 10 min) on top of the per-account lockout; answers `429 too_many_attempts`. **Single instance only** — multi-instance needs Redis |
| 4.4 | Scheduled publishing | BY DESIGN | No job needed: public visibility is a time predicate (`published_at <= now`), so a future-dated article appears by itself |
| 4.5 | Post-change session hygiene | CI-VERIFIED | Changing a password revokes every refresh token and the next sign-in carries no gate claim |

### Still open (not started — do not claim them)

- Binary media uploads (S3/R2 presigned) and image variants; the API stores metadata only.
- News revisions/autosave history, tags and related articles.
- JWT key rotation with `kid`; optional 2FA for SUPER_ADMIN; dependency scanning in CI.
- Distributed rate limiting (Redis) for multi-instance deployments.
- Load/pen tests, and a docker image build in CI (the CI runner has no working Docker; the suite runs
  against a service PostgreSQL instead).

Suite size at last green run: **77 tests, 0 failures** (12 cricket · 11 videos/match detail · 9 auth ·
9 public news · 7 news admin · 6 SEO/homepage/media · 6 RBAC · 5 password policy · 3 audit ·
3 OpenAPI · 3 search/forced-change · 3 rate limiting).

## Blockers

1. ~~No access to the frontend repositories or the audit document.~~ **RESOLVED** — both
   repositories were made public on 2026-11-16 and were re-audited (`docs/AUDIT.md` §2–§4,
   `docs/CONTRACTS.md`). Findings: article URL is `/news/{slug}`, categories have no URL
   (`/news?filter=`), the news sitemap uses a 48h window, and the **admin panel repository is
   empty** — so this backend defines that contract.
   D1 (bare payloads + RFC 9457) and D2 (Bearer tokens held in memory, no cookies) were decided by
   the backend and are recorded with their trade-offs in `docs/CONTRACTS.md` §4 — say the word if
   you want either reversed.
2. **Sandbox cannot build or run Java.** JDK/Maven/Docker/PostgreSQL absent and Maven Central
   blocked, so verification relies on CI (green) and static validators
   (`java-parser` 101 files, `pglast` 2 migrations, YAML/XML parse, entity↔column and
   permission-constant cross-checks).
3. **CI runners have no working Docker**, so the pipeline runs the suite against an
   apt-installed PostgreSQL 16 and passes `-Dtest.database.external=true`. Locally,
   Testcontainers remains the default (`mvn verify` needs Docker). If Docker becomes
   available in CI, the same suite exercises Testcontainers unchanged.

## Next actions (in order)

1. Merge this branch; both clients can then build against a complete v1 contract.
2. Client work, in their own sessions: `sports-web-admin-panel` (see `docs/ADMIN_PANEL_BRIEF.md`) and
   `sports-web` (see `docs/WEBSITE_INTEGRATION_BRIEF.md`).
3. Remaining backend backlog, only if you want it: binary media uploads + image variants, news
   revisions/tags, JWT `kid` rotation, 2FA, Redis-backed rate limiting, load tests.
