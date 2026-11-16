# Phase 0 — Audit

Date: 2026-11-16 (re-audited after the owner made both frontend repositories public)
Branch: `arena/54d18d65-sports-web-backend` (base commit `5fd7b2d`, `main`)

## 1. Backend repository (`khatrik34/sports-web-backend`)

Inspected state of `main` at branch point:

- One commit ("Initial commit"), one file: `README.md` containing the single line
  `# sports-web-backend`.
- No build tool, no source tree, no migrations, no CI, no licence, no `.gitignore`.
- No existing code, therefore **nothing was overwritten**. The placeholder README is kept
  and extended rather than replaced with unrelated content.

Conclusion: this repository is a greenfield backend. Everything below is new code.

## 2. Public website (`khatrik34/sports-web`) — RE-AUDITED, now accessible

Originally inaccessible; the owner made both repositories public and the re-audit below replaces
the earlier "not accessible" finding. Commit inspected: `cc56fe1` (`main`), Next.js 16 App Router.

**What the site actually is:** a fully designed public UI whose data layer is still mock data.
`services/index.ts` imports `mocks/data.ts` unconditionally and exposes
`ContentService<T> { list(), get(slug) }` per domain. `lib/api.ts` is an unused generic
Zod/fetch helper and `NEXT_PUBLIC_API_BASE_URL` does not change the data source. So the *routes and
SEO* are shipped facts; the *API contract* is still open.

Facts that directly constrain this backend (details in `docs/CONTRACTS.md`):

| Fact | Evidence |
| --- | --- |
| Article URL is `/news/{slug}` | `app/sitemap.ts`, `app/news-sitemap.xml/route.ts`, canonical in `app/[...slug]/page.tsx`, share links, JSON-LD `mainEntityOfPage` |
| There is **no** category URL; the news list filters with `/news?filter=<value>` | `app/[...slug]/page.tsx` news branch |
| `/news/latest`, `/news/trending`, `/news/featured` exist as pages but currently reuse one list | same file |
| Sitemap = static section paths + `/news/{slug}` + match/series/team/player/video URLs | `app/sitemap.ts` |
| News sitemap = articles published within 48h, fields loc/title/publication_date/language | `app/news-sitemap.xml/route.ts` |
| Site name "The Boundary", origin from `NEXT_PUBLIC_SITE_URL` (default `https://boundary.example`) | `config/site.ts`, `app/layout.tsx` |
| News projection uses `excerpt/image/imageAlt/author/published/updated/readTime/body[]` | `types/index.ts` |
| Search is `noindex`; robots points at `/sitemap.xml` and `/news-sitemap.xml` | `app/robots.ts`, catch-all metadata |
| Everything else (teams, players, tournaments, matches, scorecards, commentary, points, videos, rankings) is still mock-driven | `mocks/data.ts`, `services/index.ts` |

**Backend changes this forced (shipped in this branch):** article path default changed from
`/{categorySlug}/{slug}` to `/news/{slug}`; category URLs replaced by the site's filter URL; sitemap
restricted to article entries; a news-sitemap feed added; `readTime` computed server-side.

## 3. Admin panel (`khatrik34/sports-web-admin-panel`) — RE-AUDITED, accessible but EMPTY

Commit `6c0130b` (`main`) contains a single `README.md` with the repository name and nothing else:
no framework, no routes, no auth expectations, no API client.

Consequence: **there is no existing admin contract to preserve.** The endpoints, permissions and
error codes in this backend are the de-facto contract for that panel, and the panel is free to be
built against them. The one open question that matters before it is built is the auth transport
(Bearer header today vs HttpOnly cookie + CSRF as the audit proposes) — tracked as D2 in
`docs/CONTRACTS.md`.

## 4. Audit document (`sports-web` PR #2) — RE-AUDITED, now readable

PR #2 is **merged** and contains three documents (`frontend-integration-audit.md`,
`platform-architecture-proposal.md`, `development-progress.md`). They are high quality and this
backend follows them where they are specific, with two deliberate deviations recorded in
`docs/CONTRACTS.md` §4:

1. **No response envelope** (`{success,message,data,timestamp}`): bare payloads plus RFC 9457
   `problem+json` errors with stable codes. Reversible, documented, awaiting a product call (D1).
2. **Bearer tokens, not cookies yet**: the proposal's HttpOnly-cookie + CSRF design is the safer end
   state for the admin panel and is planned for Phase 2 rather than retrofitted later (D2). Access
   tokens already rotate, revoke and never carry secrets the server trusts without checks.

Also adopted from the proposal because it is right: public/admin DTO separation, publication policy
enforced in every query, Flyway-only migrations with `ddl-auto=validate`, PostgreSQL Testcontainers,
atomic last-super-admin protection, audit trail with actor/target/before-after, fail-closed
configuration, and "no live score integration claims".

## 5. Runtime / build environment in this sandbox

| Capability | Status | Impact |
| --- | --- | --- |
| JDK 21 | **absent** (`java`, `javac` not installed; no `/usr/lib/jvm`) | cannot compile |
| Maven / Gradle | **absent** | cannot build |
| Maven Central (`repo1.maven.org`, `repo.maven.apache.org`) | **blocked** (connection failed) | cannot resolve Spring Boot dependencies |
| `deb.debian.org` (apt) | **blocked** | cannot install a JDK/Maven |
| Adoptium API / GitHub release assets (`objects.githubusercontent.com`) | **blocked** | cannot download a JDK tarball |
| Docker | **absent** | Testcontainers cannot run here |
| PostgreSQL server | **absent** | migrations cannot be applied here |
| `github.com`, `codeload.github.com`, `api.github.com`, `registry.npmjs.org`, `pypi.org` | reachable | repo I/O, and non-Java syntax validators |

**Honest consequence:** I cannot compile, run, or execute tests for Java in this sandbox.
Verification of Phase 1 therefore rests on:

1. **Static validators I can run here:** Java syntax parse of every source file
   (`java-parser` from npm), PostgreSQL syntax parse of every Flyway migration
   (`pglast`, a real libpg_query parser), plus XML/YAML well-formedness of `pom.xml`,
   `application*.yml` and the CI workflow.
2. **CI on push** (`.github/workflows/ci.yml`): JDK 21 + `mvn -B verify` + Testcontainers
   PostgreSQL — the actual compile and integration tests. This is the authoritative gate.
3. Manual review against Spring Boot 3.3 / Spring Security 6 API surface.

No feature in `docs/PROGRESS.md` is marked "verified" until CI is green. See that file
for the per-item status and the exact wording of the caveat.

## 6. Findings that shaped the design

1. **Two clients, one backend.** No public sign-up, no third-party consumers, so the
   API is split by audience: `/api/v1/public/**` (anonymous, read-only, published only),
   `/api/v1/admin/**` (JWT + fine-grained permissions), `/api/v1/auth/**` (token issuing).
2. **Draft leakage is the top risk.** Enforced structurally: public endpoints do not share
   DTOs, services or query builders with admin endpoints, and the public predicate is
   `status = 'PUBLISHED' AND published_at <= now()` (so a future `published_at` is a
   scheduled article and is invisible too). Covered by an integration test.
3. **Permissions must live on the server.** Every admin handler carries
   `@PreAuthorize` with a permission authority; the JWT carries authorities but the
   *decision* is enforced per-endpoint, not by the UI.
4. **No secrets in code.** No default JWT secret, no default password, no seeded user.
   Startup fails fast if the JWT secret is missing/short. The first admin is created only
   from explicit environment variables (`APP_BOOTSTRAP_*`), opt-in.
5. **URL/SEO preservation now rests on verified facts** (`/news/{slug}`, `/news?filter=`,
   sitemap and news-sitemap fields) instead of assumptions; the article template stays
   configurable and a stored canonical path always wins over the template.
6. **No real-time cricket data.** Nothing in this phase models or claims live feeds;
   scores/commentary will be modelled as stored, admin-entered data in a later phase.
