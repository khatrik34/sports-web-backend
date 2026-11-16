# Contracts: backend ↔ existing website

Facts below were read from the actual repositories, not assumed:

| Source | Commit inspected | What it is |
| --- | --- | --- |
| `khatrik34/sports-web` | `cc56fe1` (`main`) | Next.js 16 App Router public site, currently served entirely from `mocks/data.ts` via `services/index.ts` |
| `khatrik34/sports-web-admin-panel` | `6c0130b` (`main`) | **Empty** — a `README.md` only. No admin code, no routes, no API expectations. |
| `sports-web` PR #2 | merged | `docs/frontend-integration-audit.md`, `docs/platform-architecture-proposal.md`, `docs/development-progress.md` — the design documents this backend answers to |

So: the website's **URLs are ship-state facts** (must be preserved), while the **API shapes are
proposals** (nothing is wired yet — `lib/api.ts` is an unused generic fetcher and
`NEXT_PUBLIC_API_BASE_URL` does not change the data source).

## 1. Public URL map (must stay stable)

Read from `app/[...slug]/page.tsx`, `app/sitemap.ts`, `app/news-sitemap.xml/route.ts`,
`app/layout.tsx`.

| Website URL | Backend source today | Status |
| --- | --- | --- |
| `/news` (+ `?filter=`) | `GET /api/v1/public/news`, `GET /api/v1/public/categories` | ✅ served |
| `/news/{slug}` | `GET /api/v1/public/news/{slug}` — canonical path `/news/{slug}` | ✅ served |
| `/news/latest`, `/news/featured` | `GET /api/v1/public/news/latest?featured=` | ✅ served |
| `/news/trending` | — | ⏳ needs view/engagement data (Phase 2); the site currently reuses the same list |
| `/sitemap.xml` | `GET /api/v1/public/sitemap` (article entries) | ✅ served |
| `/news-sitemap.xml` | `GET /api/v1/public/news-sitemap?hours=48` | ✅ served |
| `/search?q=` | `GET /api/v1/public/news?q=` (news only) | ⏳ cross-section search needs teams/players/etc. (Phase 3) |
| `/teams`, `/teams/{slug}` | `GET /api/v1/public/teams`, `/teams/{slug}` | ✅ served |
| `/players/{slug}` | `GET /api/v1/public/players/{slug}` (+ `?team=`) | ✅ served |
| `/cricket/fixtures`, `/cricket/live-scores` | `GET /api/v1/public/matches?filter=…` | ✅ served |
| `/cricket/match/{slug}` | `GET /api/v1/public/matches/{slug}` | ✅ served (summary; scorecard/commentary tabs come in Phase 3) |
| `/cricket/series`, `/cricket/series/{slug}` | `GET /api/v1/public/tournaments`, `/{slug}` | ✅ served |
| `/cricket/series/{slug}?filter=points-table` | `GET /api/v1/public/tournaments/{slug}/standings` | ✅ served |
| `/rankings` | `GET /public/rankings?format=` (current series' table) | ✅ served |
| `/` homepage blocks | `GET /public/homepage` | ✅ served |
| 404 / moved URLs | `GET /public/redirects?path=` (301/302/307/308/410) | ✅ served |
| `/videos`, `/videos/{slug}` | `GET /public/videos?filter=`, `/videos/{slug}` | ✅ served |
| match page `?tab=scorecard|live-commentary` | `GET /public/matches/{slug}/scorecard`, `/commentary` | ✅ served |
| `/cricket` (hub), `?filter=top-scorers|top-wickets|squads` | — | ⏳ derived data, no endpoint yet |
| `/about`, `/contact` | not content-managed | stays static |

Notes that changed the backend:

- **There is no `/category/{slug}` page.** Categories are display labels and the listing filter is
  `/news?filter=<value>` (lower-cased substring match against the category label). The backend
  therefore returns `path = /news?filter=<slug>` for a category and never invents a category URL.
- **Canonical shape is `/news/{slug}`.** `app/articlePathTemplate` defaults to that; a stored
  `canonical_path` still wins, so re-slugging never breaks an indexed URL.
- `metadataBase` is `NEXT_PUBLIC_SITE_URL`; all absolute URLs the API returns come from
  `APP_PUBLIC_SITE_BASE_URL` and must be the same origin.

## 2. News payload ↔ existing UI projection

The UI type in `types/index.ts` is a *view model*, so the frontend keeps its own Zod mapping
(as its audit already states). This table is that mapping — the adapter is mechanical:

| `NewsArticle` (UI) | Backend public field | Notes |
| --- | --- | --- |
| `slug` | `slug` | identical |
| `title` | `title` | identical |
| `category` | `categoryName` | display label (`TEST CRICKET` vs stored `Test Cricket` — render as-is) |
| `excerpt` | `summary` | rename only |
| `image` | `coverImageUrl` | backend may return absolute URLs |
| `imageAlt` | `coverImageAlt` | rename only |
| `author` | `authorDisplayName` | byline snapshot, never a user record |
| `published` | `publishedAt` | ISO-8601 UTC |
| `updated` | `updatedAt` | ISO-8601 UTC |
| `readTime` | `readTime` | computed server-side (~200 wpm, min 1) |
| `body` (paragraphs) | `body` + `bodyFormat` | backend stores one document (`HTML`/`MARKDOWN`/`PLAIN`); the site splits/renders it |
| `featured` | `featured` | identical |

SEO block (`seo` on the detail payload) maps directly onto the site's existing metadata and
JSON-LD: `metaTitle`→`title`, `metaDescription`→`description`, `canonicalUrl`→`alternates.canonical`,
`og*`→`openGraph`, `robots`→`robots`.

`GET /api/v1/public/news` returns
`{ content, page, size, totalElements, totalPages, first, last }`.

## 3. Sitemap feeds

`GET /api/v1/public/sitemap` → article entries only; static section paths stay owned by the site
(it already hardcodes them in `app/sitemap.ts`).

```json
[{ "loc": "https://example.com/news/india-australia-test-fightback",
   "lastModified": "2026-11-13T11:00:00Z", "changeFrequency": "daily", "priority": 0.8 }]
```

`GET /api/v1/public/news-sitemap?hours=48` → Google News feed, window bounded to 1–168 h:

```json
[{ "loc": "https://example.com/news/india-australia-test-fightback",
   "title": "A fightback for the ages: India stand tall at the MCG",
   "publishedAt": "2026-11-13T09:30:00Z", "language": "en" }]
```

Both feeds only ever contain published, past-dated articles (same predicate as the rest of the
public API), so a draft cannot leak through a sitemap.

## 3a. Cricket payload ↔ existing UI projection

The same rule applies: the UI types are view models and the adapter stays on the site. Exact mapping
for the shapes in `types/index.ts`:

**`Team`** ← `PublicTeam`: `slug`, `name`, `short`←`shortName`, `flag`, `description`. Extra:
`category` (`"International"|"League"|"Domestic"`), `countryCode`, `logoUrl`, `path`, `url`.

**`Match`** ← `PublicMatch`: `slug`, `tournament`←`tournamentName`, `format`, `category`,
`venue`, `date`←`startTime`, **`status`←`status` (already `live|upcoming|recent`)**, `team1`/`team2`←
`PublicTeamRef` (add `description: ''` or fetch the team page), `score1`←`team1Score`,
`score2`←`team2Score`, `note`. Extra: `state` (stored `SCHEDULED|LIVE|COMPLETED|ABANDONED`),
`resultText`, `matchNumber`, `path`, `url`.

**`Tournament`** ← `PublicTournament`: `slug`, `name`, `short`←`shortName`, `description`,
`dates`←`datesText`, **`teams`←`teamSlugs`** (array of slugs, exactly what the page compares against).
Extra: `format`, `category`, `season`, `host`, `startDate`, `endDate`, `status`, `logoUrl`, `path`, `url`.

**`Player`** ← `PublicPlayer`: `slug`, `name`←`name`, `country`, `role`, `batting`←`battingStyle`,
`bowling`←`bowlingStyle`, `description`. Extra: `team` (`PublicTeamRef`), `bornOn`, `imageUrl`, `path`, `url`.

**`PointsTable`** ← `PublicStandingRow`: `team`←`team.name`, `played`, `won`, `lost`, `points`,
`nrr` (stored as display text, so `"75.00%"` and `"+1.245"` both survive). Extra: `position`, `tied`,
`noResult`, `team.slug`.

Filter vocabulary is identical to the site's own: `?filter=live|upcoming|recent` for the phase and
`?filter=international|domestic|league` for the category on the same parameter, plus `?team=`,
`?tournament=`, `?date=yyyy-MM-dd`, `?format=`.

## 4. Decisions (taken by the backend, recorded here)

| # | Question | Backend today | Recommendation |
| --- | --- | --- | --- |
| D1 | Response envelope `{success,message,data,timestamp}` vs bare payloads + RFC 9457 errors | **DECIDED: bare payloads.** Errors are `application/problem+json` with a stable `code`. | Errors carry everything an envelope would, the format is standard, and OpenAPI stays simple. Reversible with a controller-level wrap if you ever want the envelope. |
| D2 | Auth transport: HttpOnly cookies + CSRF vs `Authorization: Bearer` | **DECIDED: Bearer access token (15 min) + rotating refresh token, both held in memory by the admin panel; refresh tokens hashed at rest and revocable.** | No cookies means no CSRF surface and no cross-site cookie problems, and the panel is built by us so it never writes tokens to `localStorage`. Cost: XSS could read a token — mitigated by CSP + never rendering raw HTML from the API. If you later host the panel on a different origin and prefer cookies, that is a contained change to `AuthController` + `SecurityConfig`. |
| D3 | Pagination field names | **DECIDED:** `page`, `size`, `totalElements`, `totalPages`, `first`, `last` | The UI's unused `PaginatedResponse` (`data/page/pageSize/total`) is scaffolding and needs remapping either way. |
| D4 | `readTime` computed server-side vs client-side | **DECIDED: server-side** | Every client agrees, and the value is also needed for sitemaps. |

## 5. Public / admin endpoint summary (Phase 1–2)

| Area | Public (anonymous) | Admin |
| --- | --- | --- |
| Videos | `GET /public/videos`, `/videos/{slug}` | `/admin/videos` (`videos:read` / `videos:write`) |
| Match detail | `GET /public/matches/{slug}/scorecard`, `/commentary`, `GET /public/rankings` | `/admin/matches/{id}/scorecard`, `/commentary` (`cricket:*`) |
| Homepage | `GET /public/homepage` | `GET/PUT /admin/homepage` (`homepage:read` / `homepage:write`) |
| SEO | `GET /public/redirects?path=` | `/admin/redirects` (`seo:read` / `seo:write`) |
| Media | — (assets referenced by URL) | `/admin/media` (`media:read` / `media:write`), metadata only |
| Auth | `POST /auth/login`, `/refresh`, `/logout` | `POST /auth/password` (self-service), `GET /auth/me` |
| --- | --- | --- |
| News | `GET /public/news`, `/news/{slug}`, `/news/latest`, `/categories`, `/sitemap`, `/news-sitemap` | `GET/POST/PUT /admin/news`, `/publish`, `/unpublish`, `/archive`; `/admin/news-categories` |
| Teams | `GET /public/teams`, `/teams/{slug}` | `GET/POST/PUT /admin/teams`, `/{id}/activate`, `/{id}/deactivate`, `DELETE /{id}` |
| Players | `GET /public/players`, `/players/{slug}` | `GET/POST/PUT /admin/players`, `/{id}/activate`, `/{id}/deactivate` |
| Series | `GET /public/tournaments`, `/{slug}`, `/{slug}/standings` | `GET/POST/PUT /admin/tournaments`, `/{id}/standings` (PUT = full replace) |
| Fixtures | `GET /public/matches`, `/matches/{slug}` | `GET/POST/PUT /admin/matches`, `/{id}/show`, `/{id}/hide`, `DELETE /{id}` |

## 5a. Page-level hand-off

`docs/WEBSITE_INTEGRATION_BRIEF.md` is the copy-paste brief for the website session: run
instructions, a page→endpoint table, response→UI mapping and known gaps. Give that file to the website
chat; this document stays the authoritative contract.

## 6. What the website still has to do (its side of the contract)

- Replace mock imports behind `services/index.ts` with server-side fetchers; keep mocks only behind
  an explicit flag and **never** fall back to mocks on API errors (silently serving fiction is worse
  than an error page).
- Browser code must call same-origin Next.js routes/proxies, not the API host directly and never
  `localhost`.
- Fix the audit's own findings before switching data source: `app/page.tsx` indexes `articles[0]`/
  `articles[4]` without guards, `/news/trending|latest|featured` share one list, and search preloads
  every collection.
- The match page's `Summary` tab currently hard-codes one slug (`featured`); with real data it must
  call `/public/matches/{slug}` for every fixture.
