# sports-web-backend

Modular monolith backend (Java 21 · Spring Boot 3.3 · Spring Security · Spring Data JPA ·
PostgreSQL 16 · Flyway · JWT · OpenAPI) serving **both** the public sports website and the
separate admin panel from one deployable.

> **Status:** Phase 1 (foundation, authentication, RBAC, users/roles, audit trail, news
> lifecycle, public read API, sitemaps, OpenAPI) is implemented on this branch and green in CI —
> 71 integration tests against PostgreSQL 16. Public URLs match the shipped website
> (`/news/{slug}`, `/news?filter=`) — see `docs/CONTRACTS.md`. `docs/PROGRESS.md` lists exactly what is
> verified and what is still open; read it before treating anything as production-ready.

## Documentation

| Document | Contents |
| --- | --- |
| [`docs/AUDIT.md`](docs/AUDIT.md) | State of this repo and what could/could not be inspected; sandbox limitations |
| [`docs/PLAN.md`](docs/PLAN.md) | Architecture, API conventions, security model, phase plan, assumptions |
| [`docs/CONTRACTS.md`](docs/CONTRACTS.md) | Verified URLs of the live site, DTO↔UI mapping, endpoint summary, decisions |
| [`docs/WEBSITE_INTEGRATION_BRIEF.md`](docs/WEBSITE_INTEGRATION_BRIEF.md) | Hand-off brief for wiring the public site to the API |
| [`docs/ADMIN_PANEL_BRIEF.md`](docs/ADMIN_PANEL_BRIEF.md) | Hand-off brief for building the admin panel |
| [`docs/PROGRESS.md`](docs/PROGRESS.md) | Per-item status, verification results, blockers, next steps |

## Requirements

- JDK 21
- Maven 3.9+ (no wrapper is committed)
- Docker (PostgreSQL for the test suite via Testcontainers; also used by `docker-compose`)

## Quick start (local)

```bash
cp .env.example .env
# generate a signing key and paste it into APP_JWT_SECRET in .env
openssl rand -base64 48

# 1. start PostgreSQL only
docker compose up -d postgres

# 2. run the API against it
set -a && . ./.env && set +a
export DB_URL="jdbc:postgresql://localhost:5432/${DB_NAME}"
mvn spring-boot:run
```

The application **fails fast** if `APP_JWT_SECRET` is missing, not base64, or shorter than
32 bytes. That is intentional: there is no fallback secret anywhere in the code.

Create the first administrator once (no credentials are seeded by migrations):

```bash
export APP_BOOTSTRAP_ENABLED=true
export APP_BOOTSTRAP_ADMIN_EMAIL=you@example.com
export APP_BOOTSTRAP_ADMIN_PASSWORD='a-strong-password-12+chars'
mvn spring-boot:run            # creates the account only if the user table is empty
# then set APP_BOOTSTRAP_ENABLED=false again
```

Full stack in containers:

```bash
docker compose up --build
```

## Try it

```bash
# sign in
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"you@example.com","password":"a-strong-password-12+chars"}'

# public feed (no auth, published articles only)
curl -s 'http://localhost:8080/api/v1/public/news?size=5'

# staff endpoint (permission checked server side)
curl -s http://localhost:8080/api/v1/admin/news -H "Authorization: Bearer $ACCESS_TOKEN"
```

OpenAPI JSON: `http://localhost:8080/v3/api-docs` · Swagger UI: `http://localhost:8080/swagger-ui.html`
(set `APP_DOCS_PUBLIC=false` in production).

## Tests

```bash
mvn -B verify
```

Integration tests boot the real application against **PostgreSQL 16** (Flyway migrations +
Hibernate schema validation + SQL behaviour), and drive the actual HTTP/security stack
through MockMvc. Docker must be running because the database is a Testcontainers container.

No Docker? Point the suite at an existing PostgreSQL instead:

```bash
mvn -B verify -Dtest.database.external=true \
  -Dspring.datasource.url=jdbc:postgresql://localhost:5432/sportsweb \
  -Dspring.datasource.username=sportsweb -Dspring.datasource.password=sportsweb
```

CI uses exactly this mode (GitHub runners here have no working Docker) with an
apt-installed PostgreSQL 16.

Covered explicitly: draft/archived/scheduled articles never reach the public API, missing
permission → 403, missing token → 401, refresh-token rotation and reuse detection, account
lockout, audit entries for failed sign-in and publishing, slug conflicts, password policy.

## API surface (v1)

| Area | Endpoint | Access |
| --- | --- | --- |
| Auth | `POST /api/v1/auth/login` · `POST /api/v1/auth/refresh` · `POST /api/v1/auth/logout` | anonymous (possession of the refresh token is the credential) |
| Auth | `GET /api/v1/auth/me` | any valid access token |
| Public · videos | `GET /api/v1/public/videos` · `/videos/{slug}` | anonymous, **published only** |
| Public · match detail | `GET /api/v1/public/matches/{slug}/scorecard` · `/commentary` · `/rankings?format=` · `/homepage` · `/redirects?path=` | anonymous |
| Public · cricket | `GET /api/v1/public/teams` · `/teams/{slug}` · `/players` · `/players/{slug}` · `/tournaments` · `/tournaments/{slug}` · `/tournaments/{slug}/standings` · `/matches` · `/matches/{slug}` | anonymous, **active/hidden rules enforced** |
| Public | `GET /api/v1/public/news` · `/news/latest` · `/news/{slug}` · `/categories` · `/sitemap` · `/news-sitemap?hours=48` | anonymous, **published only** |
| Admin news | `GET/POST /api/v1/admin/news`, `GET/PUT /api/v1/admin/news/{id}`, `POST .../publish`, `.../unpublish`, `.../archive` | `news:read`, `news:write`, `news:publish` |
| Admin categories | `GET/POST /api/v1/admin/news-categories`, `PUT /{id}` | `news:read`, `news:write` |
| Admin users | `GET/POST /api/v1/admin/users`, `GET/PATCH /{id}`, `PUT /{id}/roles`, `POST /{id}/password`, `POST /{id}/unlock` | `users:read`, `users:manage` |
| Admin roles | `GET /api/v1/admin/roles`, `GET /api/v1/admin/permissions` | `roles:read` |
| Admin audit | `GET /api/v1/admin/audit-logs` | `audit:read` |

Roles seeded by migration: `SUPER_ADMIN`, `EDITOR`, `REPORTER`, `AUDITOR`.
Permissions: `news:read`, `news:write`, `news:publish`, `users:read`, `users:manage`,
`roles:read`, `audit:read`. Endpoints check **permissions**, never role names.

Errors are RFC 9457 `application/problem+json` with a stable `code` field.

## Configuration

Everything is environment driven; `src/main/resources/application.yml` contains defaults
only where a default is safe.

| Variable | Required | Purpose |
| --- | --- | --- |
| `APP_JWT_SECRET` | **yes** | Base64 HMAC key, ≥ 32 bytes decoded (`openssl rand -base64 48`) |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | yes in practice | PostgreSQL connection |
| `APP_CORS_ALLOWED_ORIGINS` | yes | Website + admin panel origins, comma separated |
| `APP_PUBLIC_SITE_BASE_URL` | yes | Absolute base for `url`/canonical/sitemap in public payloads |
| `APP_ARTICLE_PATH_TEMPLATE` | no | Default `/news/{slug}` (verified against the live site) |
| `APP_PUBLIC_SITE_LANGUAGE` | no | Default `en`; used by the news sitemap feed |
| `APP_ACCESS_TOKEN_TTL` / `APP_REFRESH_TOKEN_TTL` | no | Defaults `PT15M` / `P14D` |
| `APP_LOGIN_MAX_FAILED_ATTEMPTS` / `APP_LOGIN_LOCKOUT_DURATION` | no | Defaults `5` / `PT15M` |
| `APP_BOOTSTRAP_*` | no | One-off first administrator creation |
| `APP_DOCS_PUBLIC` | no | `false` disables anonymous Swagger/OpenAPI |

## Security behaviour worth knowing

- Access tokens are stateless JWTs (15 min). Refresh tokens are opaque, stored **hashed**,
  rotated on every refresh, and revoked on logout, password reset, account disable, or
  whenever a revoked token is presented again (theft assumption → all sessions revoked).
- Consequence: changing a user's roles takes effect on their next refresh (≤ 15 min).
  Disabling an account also immediately revokes its refresh tokens.
- Five failed sign-ins lock the account for 15 minutes; every attempt is audited.
- Passwords: BCrypt (cost 12 in production), policy ≥ 12 chars with 3 of 4 character classes.
- Audit trail is append-only, records actor/IP/user-agent plus a compact before/after
  snapshot (never password material), and is written in a separate transaction for security
  events so failures cannot erase it.
- `X-Forwarded-For` is trusted for IP logging: deploy behind a proxy that overwrites it.
- CSRF is disabled because no cookie-based session exists; tokens travel in the
  `Authorization` header.

## Repository layout

```
src/main/java/com/sportsweb/backend
├── common        error handling (problem+json), paging, audit trail, slug utils
├── config        typed configuration properties (@ConfigurationProperties), OpenAPI
├── security      JWT, login/refresh/logout, users, roles, permissions, bootstrap
└── news          articles, categories, editorial workflow, public read API
src/main/resources/db/migration   Flyway migrations (V1 schema, V2 roles/permissions)
src/test/java                     Testcontainers PostgreSQL + MockMvc integration tests
```

## Not implemented yet (see `docs/PLAN.md`)

Media/uploads, redirect/URL migration, news revisions, teams/players/tournaments/fixtures/
scores/commentary/points tables, videos, homepage configuration, login rate limiting,
2FA, JWT key rotation. **No external cricket data integration is claimed or planned** in
these phases — sports data will be admin-entered.
