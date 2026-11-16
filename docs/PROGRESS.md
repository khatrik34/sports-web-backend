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

## Phase 2 — Media, SEO completion, revisions — OPEN

No longer blocked by missing URL facts: the URL inventory is verified and the admin panel has no
contract of its own to preserve. Waiting only on decisions D1/D2. Work that can start immediately:
- `redirect` table + 301/410 handling and a real `sitemap.xml` renderer.
- Change-password endpoint (unblocks enforcing `mustChangePassword`).
- Optional: contract snapshot of `/v3/api-docs` as a CI check.

## Phase 3 — Teams, players, tournaments, fixtures, scores, commentary, points tables — OPEN

Designed in `docs/PLAN.md`; nothing implemented. No external/live cricket data will be
consumed or claimed — sports data is admin-entered.

## Phase 4 — Videos, homepage configuration, hardening — OPEN

Per-IP login throttling, JWT key rotation (`kid`), optional 2FA for `SUPER_ADMIN`,
audit retention/export, media storage, homepage blocks, videos.

## Blockers

1. ~~No access to the frontend repositories or the audit document.~~ **RESOLVED** — both
   repositories were made public on 2026-11-16 and were re-audited (`docs/AUDIT.md` §2–§4,
   `docs/CONTRACTS.md`). Findings: article URL is `/news/{slug}`, categories have no URL
   (`/news?filter=`), the news sitemap uses a 48h window, and the **admin panel repository is
   empty** — so this backend defines that contract.
   Remaining decisions needed from you: **D1** (response envelope vs bare payloads) and **D2**
   (Bearer tokens now vs HttpOnly cookie + CSRF for the admin panel).
2. **Sandbox cannot build or run Java.** JDK/Maven/Docker/PostgreSQL absent and Maven Central
   blocked, so verification relies on CI (green) and static validators
   (`java-parser` 101 files, `pglast` 2 migrations, YAML/XML parse, entity↔column and
   permission-constant cross-checks).
3. **CI runners have no working Docker**, so the pipeline runs the suite against an
   apt-installed PostgreSQL 16 and passes `-Dtest.database.external=true`. Locally,
   Testcontainers remains the default (`mvn verify` needs Docker). If Docker becomes
   available in CI, the same suite exercises Testcontainers unchanged.

## Next actions (in order)

1. Decide D1/D2 (envelope, auth transport) → then freeze v1 contracts with a checked-in
   OpenAPI snapshot before the admin panel is built against them.
2. Phase 2 media library and redirect/SEO tooling; add change-password endpoint; wire the
   website's adapters in `services/index.ts` (its side of the contract, `docs/CONTRACTS.md` §5).
3. Phase 3 sports data model (teams/players/tournaments/fixtures/scores/commentary/points).
4. Harden: per-IP throttling, refresh-token family table cleanup job tuning, JWT `kid`
   rotation, 2FA for super admins, dependency scanning in CI.
