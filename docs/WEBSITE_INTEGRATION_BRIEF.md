# Brief: wiring the website (`khatrik34/sports-web`) to the backend

Self-contained hand-off for the session that replaces `services/index.ts` mocks with real API calls.
Backend: this repository, `main`, **71 integration tests green**.

## How to run the backend locally

```bash
cp .env.example .env      # DB_PASSWORD + APP_JWT_SECRET (openssl rand -base64 48)
docker compose up -d db   # PostgreSQL 16
mvn spring-boot:run       # http://localhost:8080
```

Set `APP_PUBLIC_SITE_BASE_URL=http://localhost:3000` so the `url` fields the API returns match the site.
The site must call the API from **server components / route handlers**, never from the browser, and
must not hardcode the host in client code (`API_BASE_URL` env, server-side only).

## Page → endpoint map

| Website page | Calls |
| --- | --- |
| `/` home | `GET /api/v1/public/homepage` → for each block fetch what its `config` references (see below). Fallback when no blocks are configured: `/public/news/latest?featured=true`, `/public/matches?filter=live`, `/public/videos`. |
| `/news` (+ `?filter=`) | `GET /api/v1/public/news?category={filter}&page=0&size=12`, `GET /api/v1/public/categories` for the filter bar |
| `/news/{slug}` | `GET /api/v1/public/news/{slug}`; "more stories" = `/public/news?size=3` |
| `/teams` | `GET /api/v1/public/teams?size=100` |
| `/teams/{slug}` | `GET /public/teams/{slug}`, `GET /public/players?team={slug}`, `GET /public/matches?team={slug}` |
| `/players/{slug}` | `GET /api/v1/public/players/{slug}` |
| `/cricket/live-scores`, `/cricket/fixtures` | `GET /public/matches?filter={all|live|upcoming|recent|international|domestic|league}&team=&tournament=&date=&from=&to=&size=20` (see the fixture filter rules below) |
| `/cricket/match/{slug}` | `GET /public/matches/{slug}`; tab `scorecard` → `GET /public/matches/{slug}/scorecard`; tab `live-commentary` → `GET /public/matches/{slug}/commentary?size=20`; tab `points-table` → `GET /public/tournaments/{tournamentSlug}/standings` |
| `/cricket/series` | `GET /api/v1/public/tournaments` |
| `/cricket/series/{slug}` | `GET /public/tournaments/{slug}`, `GET /public/tournaments/{slug}/standings`, `GET /public/matches?tournament={slug}` |
| `/rankings` | `GET /api/v1/public/rankings?format=TEST` (omit `format` for the current series) |
| `/videos` (+ `?filter=`) | `GET /api/v1/public/videos?filter={category}&size=12` |
| `/videos/{slug}` | `GET /api/v1/public/videos/{slug}` |
| `/search?q=` | `GET /api/v1/public/search?q={q}&limit=5` — one call; each hit is `{ type, title, subtitle, path, url }` and `type` is `news`, `match`, `series`, `team`, `player` or `video` |
| `sitemap.xml` | keep the static section paths, append `GET /public/sitemap` and each `/public/videos?size=100` entry |
| `news-sitemap.xml` | `GET /api/v1/public/news-sitemap?hours=48` |
| 404 handler | `GET /api/v1/public/redirects?path={requestedPath}` → 301/302/307/308 redirect to `toPath`, or 410 when retired; 404 = genuinely gone |

## Response shapes → the site's own types

`docs/CONTRACTS.md` §2 and §3a hold the exact field-by-field mapping (`excerpt↔summary`,
`image↔coverImageUrl`, `short↔shortName`, `dates↔datesText`, `teams↔teamSlugs`,
`batting↔battingStyle`, `image↔thumbnailUrl`, `duration↔durationText`, `score1↔team1Score`, …).

New since the first brief:

- **Match centre**: `GET /public/matches/{slug}/scorecard` returns innings, each with `batting[]`
  (`batter`, `dismissal`, `runs`, `balls`, `fours`, `sixes`, `notOut`) and `bowling[]`
  (`bowler`, `overs`, `maidens`, `runsConceded`, `wickets`, `economy`). Commentary entries are
  `{ over, kind, text, createdAt }`, newest first, paged.
- **Videos**: `{ slug, title, category, image, duration, description, videoUrl, published, path, url }`.
  Playback is the site's job; the API only stores the embed URL.
- **Homepage**: blocks are `{ key, type, title, sortOrder, config }` where `type` is one of
  `HERO`, `HEADLINE_LIST`, `MATCH_STRIP`, `POINTS_TABLE`, `VIDEO_RAIL`, `CUSTOM`. `config` carries
  references, e.g. `{"articleSlugs":["a","b"],"limit":3}` or `{"tournamentSlug":"wtc-2026"}`.
  Render unknown types as skipped blocks so new block types never break the page.
- **Rankings**: `{ tournamentName, tournamentSlug, format, category, status, rows[] }` where a row is
  the same shape the points table already renders.

## Rules

1. Do not fall back to mocks on API errors — a real error page beats silent fiction.
2. Fetch server-side; the browser must talk only to the site's own origin (route handlers/proxies).
3. Keep the existing URLs and SEO metadata; use the API's `seo` block and `path`/`url` fields.
4. `next: { revalidate: 60 }` (or ISR) for lists, `no-store` for live scores — the backend does not
   push updates, so nothing here is real-time.
5. Validate the API payloads at the adapter boundary (Zod is already a dependency) so a backend change
   fails loudly in CI instead of rendering blanks.

## Known gaps (do not invent)

- No live-score feed (scores are staff-entered); nothing here is real-time.
- Sign-in throttling answers `429 too_many_attempts` — surface it as "try again shortly".
- Media is metadata only (no binary upload, no image resizing service).
- `mustChangePassword` is exposed on the user payload but the API does not block requests while true.
