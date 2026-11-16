# Phase 0 — Audit

Date: 2026-11-16
Branch: `arena/54d18d65-sports-web-backend` (base commit `5fd7b2d`, `main`)

## 1. Backend repository (`khatrik34/sports-web-backend`)

Inspected state of `main` at branch point:

- One commit ("Initial commit"), one file: `README.md` containing the single line
  `# sports-web-backend`.
- No build tool, no source tree, no migrations, no CI, no licence, no `.gitignore`.
- No existing code, therefore **nothing was overwritten**. The placeholder README is kept
  and extended rather than replaced with unrelated content.

Conclusion: this repository is a greenfield backend. Everything below is new code.

## 2. Public website (`khatrik34/sports-web`) — NOT ACCESSIBLE

| Probe | Result |
| --- | --- |
| `https://github.com/khatrik34/sports-web` | HTTP 404 (private or non-existent to this token) |
| `https://api.github.com/repos/khatrik34/sports-web` | HTTP 403 `Resource not accessible by integration` |
| `git ls-remote https://github.com/khatrik34/sports-web.git` | HTTP 403 |
| `gh repo view khatrik34/sports-web` | `Resource not accessible by integration` |
| `https://codeload.github.com/khatrik34/sports-web/tar.gz/refs/heads/main` | HTTP 404 |

Control probe: `codeload.github.com/khatrik34/sports-web-backend` → HTTP 200, so the
token does work; the sandbox integration is scoped to this one repository only.

## 3. Admin panel (`khatrik34/sports-web-admin-panel`) — NOT ACCESSIBLE

Same probes, same results (404 / 403). No code, no route list, no auth expectations.

## 4. Audit document (`sports-web` PR #2) — NOT ACCESSIBLE

`gh pr view 2 --repo khatrik34/sports-web` → `Resource not accessible by integration`.
No audit document, no proposed architecture, no URL inventory, no field contracts.

### What this means (stated plainly, not guessed)

I could not read the frontend audit, the website source, or the admin panel. Therefore:

- I have **not** invented current public URL patterns, sitemap rules, category names,
  field names the frontend already expects, or the admin panel's route/state model.
- Anything that depends on those facts is treated as a **named assumption** in
  `docs/PLAN.md`, kept **configurable**, and marked open in `docs/PROGRESS.md`.
- Phase 1 was chosen so that it is useful and correct **without** those documents:
  authentication, RBAC, users, audit trail, news lifecycle, draft protection.

**Needed to close these gaps** (any one of these unblocks contract work):
1. Read access for this GitHub identity to `sports-web` and `sports-web-admin-panel`
   (add the app/integration to both repos), **or**
2. `docs/` (or PR #2 description) pasted/attached here, **or**
3. The public site's `sitemap.xml` + 2–3 sample article URLs + one category URL, **or**
4. Frontend HTTP client code (`src/api/*` or equivalent) — the exact request/response
   shapes the site and admin panel already use.

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
5. **URL/SEO preservation needs facts I do not have.** Mitigated with configurable
   article path template + stored `canonical_path`, `meta_*`, `og_*`, `robots`,
   `keywords`, and a public sitemap feed — all awaiting the real URL inventory.
6. **No real-time cricket data.** Nothing in this phase models or claims live feeds;
   scores/commentary will be modelled as stored, admin-entered data in a later phase.
