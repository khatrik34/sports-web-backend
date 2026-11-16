# Running everything on your own laptop

Three pieces: **PostgreSQL**, the **Java API**, and the **Next.js admin panel** (and later the public
website). Work in that order — the API creates its own schema on first start.

You already have IntelliJ IDEA, VS Code and pgAdmin. What is still needed is listed below; the short
version is **JDK 21** (not 17), **PostgreSQL 16 server** (pgAdmin is only a GUI), and **Node.js 20+**.

| Tool | Version | Why | Notes |
| --- | --- | --- | --- |
| JDK | **21** | `pom.xml` targets 21, Spring Boot 3.3 | IntelliJ can download it: *Project Structure → SDK → Add SDK → Download JDK → Temurin 21* |
| Maven | 3.9 | builds the API | **Optional** — IntelliJ bundles Maven. Install only for terminal use |
| PostgreSQL | **16** | the database | the installer includes a server; pgAdmin is only the client |
| Node.js | **20 or 22 LTS** | Next.js admin panel (15) and website (16) | `node -v` must print v20+ |
| Git | any | clone the repos | you already have it |

Docker is **not** required for local work; it is only an alternative one-command stack.

Check what you have:

```bash
java -version     # must say 21.x  -> 17 will NOT compile this project
node -v           # 20+ / 22+
git --version
```

---

## 1. Database

Install PostgreSQL 16 (remember the `postgres` superuser password and port 5432). Then create the app
role and database — the app must own the database so Flyway can create tables:

```sql
CREATE USER sportsweb WITH PASSWORD 'StrongPass123!';
CREATE DATABASE sportsweb OWNER sportsweb;
```

Run that in pgAdmin (**right-click server → Query Tool**) or `psql -U postgres`.

---

## 2. The API

```bash
git clone https://github.com/khatrik34/sports-web-backend.git
cd sports-web-backend
```

**In IntelliJ:** *File → Open* → pick the `sports-web-backend` folder (the one with `pom.xml`) → trust the
project → let Maven finish downloading. Then:

- *File → Project Structure → Project* → **SDK 21**, language level 21.
- *Settings → Build, Execution, Deployment → Build Tools → Maven* → **JDK for importer = 21**.
- Open `src/main/java/com/sportsweb/backend/SportsWebBackendApplication.java` → green ▶ → *Modify options
  → Environment variables* → paste the block below.

Generate the JWT secret first (Git Bash, which ships with Git for Windows):

```bash
openssl rand -base64 48
```

Environment variables (Windows: one line, values separated by `;` — on macOS/Linux set them with `export`):

```
DB_URL=jdbc:postgresql://localhost:5432/sportsweb
DB_USERNAME=sportsweb
DB_PASSWORD=StrongPass123!
APP_JWT_SECRET=<paste the openssl output>
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001
APP_PUBLIC_SITE_BASE_URL=http://localhost:3000
APP_BOOTSTRAP_ENABLED=true
APP_BOOTSTRAP_ADMIN_EMAIL=admin@sportsweb.local
APP_BOOTSTRAP_ADMIN_PASSWORD=AdminPassw0rd!2026
SPRING_FLYWAY_LOCATIONS=classpath:db/migration,classpath:db/demo
```

- The last line loads the demo dataset (teams, fixtures, news, videos, homepage). Remove it if you want
  an empty database; the schema is created either way.
- `APP_BOOTSTRAP_*` creates the first administrator **once**. Turn `APP_BOOTSTRAP_ENABLED` back to `false`
  after the first successful start.
- The bootstrap password must satisfy the policy: 12+ characters with upper case, lower case, a digit and
  a symbol.

**Important:** a `.env` file is used **only by docker compose**. Running from IntelliJ or `mvn` does not
read it — the variables above must be real environment variables (IDE run configuration, or `export` /
`$env:` in a terminal).

Terminal alternative:

```powershell
# PowerShell, from the repo root
$env:DB_URL="jdbc:postgresql://localhost:5432/sportsweb"; $env:DB_USERNAME="sportsweb"
$env:DB_PASSWORD="StrongPass123!"; $env:APP_JWT_SECRET="<openssl output>"
$env:APP_CORS_ALLOWED_ORIGINS="http://localhost:3000,http://localhost:3001"
$env:SPRING_FLYWAY_LOCATIONS="classpath:db/migration,classpath:db/demo"
$env:APP_BOOTSTRAP_ENABLED="true"; $env:APP_BOOTSTRAP_ADMIN_EMAIL="admin@sportsweb.local"
$env:APP_BOOTSTRAP_ADMIN_PASSWORD="AdminPassw0rd!2026"
mvn spring-boot:run
```

First build downloads several hundred MB of dependencies (3–5 minutes); later starts take seconds.

Verify:

| What | Where |
| --- | --- |
| Health | <http://localhost:8080/actuator/health> → `{"status":"UP"}` |
| Swagger UI | <http://localhost:8080/swagger-ui.html> |
| Seeded news | <http://localhost:8080/api/v1/public/news> |
| Live match | <http://localhost:8080/api/v1/public/matches?status=LIVE> |

Sign in from the command line:

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@sportsweb.local","password":"AdminPassw0rd!2026"}'
```

The response contains `accessToken` and `user.mustChangePassword: true` — the API will refuse every other
call (`403 password_change_required`) until you change it, which is exactly what the admin panel's
change-password screen does.

---

## 3. The admin panel

```bash
git clone https://github.com/khatrik34/sports-web-admin-panel.git
cd sports-web-admin-panel
npm install
```

Create `.env.local` (git-ignored):

```
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

```bash
npm run dev        # http://localhost:3000
```

Log in with the bootstrap account, set the new password, and the panel's screens (news, teams, players,
tournaments, matches, videos, homepage, redirects, users, roles, audit) talk to your local API.

The **public website** (`sports-web`, Next.js 16) also defaults to port 3000, so run only one at a time,
or `npm run dev -- -p 3001` — the API's CORS list already allows both ports.

---

## Common problems

| Symptom | Cause / fix |
| --- | --- |
| `invalid target release: 21` or `class file version` error | the IDE/Maven is using JDK 17 — set SDK **and** Maven importer JDK to 21 |
| `JWT secret must be configured` on start | `APP_JWT_SECRET` missing or shorter than 32 bytes decoded |
| `password authentication failed for user "sportsweb"` | database env values do not match the SQL you ran |
| `FATAL: database "sportsweb" does not exist` | step 1 skipped, or wrong port (5432 default) |
| `permission denied for schema public` | the database is not owned by `sportsweb` — recreate it with `OWNER sportsweb` |
| Port 8080 already in use | another process owns it; stop it or set `SERVER_PORT=8081` (and update the panel's `.env.local`) |
| Admin panel shows network errors | `.env.local` missing/typo, or the API is not running on 8080 |
| 403 on every admin call right after login | expected: `mustChangePassword` — change the password first |
| Cold start takes a minute | only on free hosting, not locally |

Useful commands:

```bash
mvn test                                   # the whole suite (needs Docker or a local test DB)
mvn -DskipTests package                    # build the jar
docker compose up -d --build               # alternative: API + PostgreSQL together
```
