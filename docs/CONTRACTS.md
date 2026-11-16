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
| `/cricket`, `/cricket/live-scores`, `/cricket/fixtures`, `/cricket/match/{slug}`, `/cricket/series[/{slug}]`, `/rankings` | — | ⏳ Phase 3 (scores/fixtures/points) |
| `/teams[/{slug}]`, `/players/{slug}` | — | ⏳ Phase 3 |
| `/videos[/{slug}]` | — | ⏳ Phase 4 |
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

## 4. Decisions still open (need a product call)

| # | Question | Backend today | Recommendation |
| --- | --- | --- | --- |
| D1 | Response envelope `{success,message,data,timestamp}` (proposed in the audit) vs bare payloads + RFC 9457 errors | bare payloads; errors are `application/problem+json` with a stable `code` | **Keep bare payloads.** Errors already carry everything an envelope would, `problem+json` is a standard, and OpenAPI stays simpler. If you prefer the envelope, it is a mechanical wrap in the controllers — say so and I will do it before Phase 2 freezes contracts. |
| D2 | Auth transport: HttpOnly cookies + CSRF (proposed in the audit) vs `Authorization: Bearer` | Bearer only, no cookies, CSRF disabled | **Cookies for the admin panel are the safer end state** (XSS cannot exfiltrate the refresh token). This needs: same-site deployment decision, `SameSite`/`Secure` values, CSRF token endpoint, and refresh-on-cookie endpoints. Worth doing in Phase 2 *before* the admin panel is built — the admin repo is empty, so nothing depends on either choice yet. |
| D3 | Pagination field names | `page`, `size`, `totalElements`, `totalPages`, `first`, `last` | Keep; the UI's unused `PaginatedResponse` (`data/page/pageSize/total`) is scaffolding and would need remapping either way. |
| D4 | `readTime` computed server-side vs client-side | server-side | Keep server-side so every client agrees. |

## 5. What the website still has to do (its side of the contract)

- Replace mock imports behind `services/index.ts` with server-side fetchers; keep mocks only behind
  an explicit flag and **never** fall back to mocks on API errors (silently serving fiction is worse
  than an error page).
- Browser code must call same-origin Next.js routes/proxies, not the API host directly and never
  `localhost`.
- Fix the audit's own findings before switching data source: `app/page.tsx` indexes `articles[0]`/
  `articles[4]` without guards, `/news/trending|latest|featured` share one list, and search preloads
  every collection.
