-- ---------------------------------------------------------------------------
-- DEMO DATA - single script for pgAdmin (Query Tool -> Open File -> F5)
--
-- GENERATED FILE: do not edit. Build from src/main/resources/db/demo/R__demo_data.sql
-- and R__demo_data_extra.sql. Safe to run more than once: fixed UUIDs plus
-- ON CONFLICT DO NOTHING mean it can never duplicate rows.
--
-- Run it against the "sportsweb" database. It creates no user accounts.
-- ---------------------------------------------------------------------------

-- Demo content for local/demo deployments.
--
-- This file lives in db/demo, NOT db/migration, so Flyway never applies it unless you explicitly
-- add the folder to SPRING_FLYWAY_LOCATIONS (see docs/DEPLOYMENT.md). Every statement is written with
-- ON CONFLICT DO NOTHING and fixed UUIDs, so applying it twice cannot duplicate rows.
--
-- It contains no secrets and no accounts: the first administrator still comes from app.bootstrap.

-- ---------------------------------------------------------------- teams
INSERT INTO cricket_team (id, slug, name, short_name, category, country_code, flag_emoji, sport, description)
VALUES
    ('11111111-1111-1111-1111-111111111101', 'pakistan', 'Pakistan', 'PAK', 'INTERNATIONAL', 'PK', '🇵🇰', 'cricket',
     'Pakistan national cricket team, all formats.'),
    ('11111111-1111-1111-1111-111111111102', 'india', 'India', 'IND', 'INTERNATIONAL', 'IN', '🇮🇳', 'cricket',
     'India national cricket team, all formats.'),
    ('11111111-1111-1111-1111-111111111103', 'australia', 'Australia', 'AUS', 'INTERNATIONAL', 'AU', '🇦🇺', 'cricket',
     'Australia national cricket team, all formats.'),
    ('11111111-1111-1111-1111-111111111104', 'england', 'England', 'ENG', 'INTERNATIONAL', 'GB', '🏴󠁧󠁢󠁥󠁮󠁧󠁿', 'cricket',
     'England national cricket team, all formats.')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- players
INSERT INTO cricket_player (id, slug, full_name, team_id, country, role, batting_style, bowling_style, description)
VALUES
    ('22222222-2222-2222-2222-222222222201', 'babar-azam', 'Babar Azam',
     '11111111-1111-1111-1111-111111111101', 'Pakistan', 'Batter', 'Right-hand bat', 'Right-arm offbreak',
     'Pakistan captain and top-order batter.'),
    ('22222222-2222-2222-2222-222222222202', 'mohammad-rizwan', 'Mohammad Rizwan',
     '11111111-1111-1111-1111-111111111101', 'Pakistan', 'Wicketkeeper Batter', 'Right-hand bat', NULL,
     'Pakistan wicketkeeper and opener.'),
    ('22222222-2222-2222-2222-222222222203', 'shaheen-afridi', 'Shaheen Afridi',
     '11111111-1111-1111-1111-111111111101', 'Pakistan', 'Bowler', 'Left-hand bat', 'Left-arm fast',
     'Pakistan new-ball bowler.'),
    ('22222222-2222-2222-2222-222222222204', 'rohit-sharma', 'Rohit Sharma',
     '11111111-1111-1111-1111-111111111102', 'India', 'Batter', 'Right-hand bat', 'Right-arm offbreak',
     'India opener and captain.'),
    ('22222222-2222-2222-2222-222222222205', 'virat-kohli', 'Virat Kohli',
     '11111111-1111-1111-1111-111111111102', 'India', 'Batter', 'Right-hand bat', 'Right-arm medium',
     'India top-order batter.'),
    ('22222222-2222-2222-2222-222222222206', 'jasprit-bumrah', 'Jasprit Bumrah',
     '11111111-1111-1111-1111-111111111102', 'India', 'Bowler', 'Right-hand bat', 'Right-arm fast',
     'India strike bowler.'),
    ('22222222-2222-2222-2222-222222222207', 'pat-cummins', 'Pat Cummins',
     '11111111-1111-1111-1111-111111111103', 'Australia', 'All-rounder', 'Right-hand bat', 'Right-arm fast',
     'Australia captain and fast bowler.'),
    ('22222222-2222-2222-2222-222222222208', 'travis-head', 'Travis Head',
     '11111111-1111-1111-1111-111111111103', 'Australia', 'Batter', 'Left-hand bat', 'Right-arm offbreak',
     'Australia top-order batter.'),
    ('22222222-2222-2222-2222-222222222209', 'mitchell-starc', 'Mitchell Starc',
     '11111111-1111-1111-1111-111111111103', 'Australia', 'Bowler', 'Left-hand bat', 'Left-arm fast',
     'Australia left-arm quick.'),
    ('22222222-2222-2222-2222-222222222210', 'joe-root', 'Joe Root',
     '11111111-1111-1111-1111-111111111104', 'England', 'Batter', 'Right-hand bat', 'Right-arm offbreak',
     'England middle-order batter.'),
    ('22222222-2222-2222-2222-222222222211', 'ben-stokes', 'Ben Stokes',
     '11111111-1111-1111-1111-111111111104', 'England', 'All-rounder', 'Left-hand bat', 'Right-arm fast-medium',
     'England all-rounder.'),
    ('22222222-2222-2222-2222-222222222212', 'jofra-archer', 'Jofra Archer',
     '11111111-1111-1111-1111-111111111104', 'England', 'Bowler', 'Right-hand bat', 'Right-arm fast',
     'England express bowler.')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- series + standings
INSERT INTO cricket_tournament (id, slug, name, short_name, description, format, category, season, host,
                                dates_text, start_date, end_date, status)
VALUES
    ('33333333-3333-3333-3333-333333333301', 'asia-cup-2026', 'Asia Cup 2026', 'Asia Cup',
     'Four-team T20I tournament used as demo content for the website and admin panel.',
     'T20I', 'INTERNATIONAL', '2026', 'Pakistan',
     '15 Nov – 30 Nov 2026', CURRENT_DATE - 1, CURRENT_DATE + 14, 'ONGOING')
ON CONFLICT DO NOTHING;

INSERT INTO cricket_tournament_team (tournament_id, team_id)
VALUES
    ('33333333-3333-3333-3333-333333333301', '11111111-1111-1111-1111-111111111101'),
    ('33333333-3333-3333-3333-333333333301', '11111111-1111-1111-1111-111111111102'),
    ('33333333-3333-3333-3333-333333333301', '11111111-1111-1111-1111-111111111103'),
    ('33333333-3333-3333-3333-333333333301', '11111111-1111-1111-1111-111111111104')
ON CONFLICT DO NOTHING;

INSERT INTO cricket_standing (id, tournament_id, team_id, position, played, won, lost, tied, no_result, points, nrr)
VALUES
    ('33333333-3333-3333-3333-333333333311', '33333333-3333-3333-3333-333333333301', '11111111-1111-1111-1111-111111111101', 1, 2, 2, 0, 0, 0, 4, '+1.245'),
    ('33333333-3333-3333-3333-333333333312', '33333333-3333-3333-3333-333333333301', '11111111-1111-1111-1111-111111111103', 2, 2, 1, 1, 0, 0, 2, '+0.412'),
    ('33333333-3333-3333-3333-333333333313', '33333333-3333-3333-3333-333333333301', '11111111-1111-1111-1111-111111111104', 3, 2, 1, 1, 0, 0, 2, '-0.208'),
    ('33333333-3333-3333-3333-333333333314', '33333333-3333-3333-3333-333333333301', '11111111-1111-1111-1111-111111111102', 4, 2, 0, 2, 0, 0, 0, '-1.377')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- fixtures
INSERT INTO cricket_match (id, slug, tournament_id, team1_id, team2_id, format, category, venue, start_time,
                           status, match_number, team1_score, team2_score, note, result_text, visible)
VALUES
    ('44444444-4444-4444-4444-444444444401', 'pakistan-vs-india-asia-cup-2026',
     '33333333-3333-3333-3333-333333333301',
     '11111111-1111-1111-1111-111111111101', '11111111-1111-1111-1111-111111111102',
     'T20I', 'INTERNATIONAL', 'Gaddafi Stadium, Lahore', now() - interval '2 hours',
     'LIVE', 'Match 3', '186/4 (18.2)', '—', 'India won the toss and elected to field.', NULL, true),
    ('44444444-4444-4444-4444-444444444402', 'australia-vs-england-asia-cup-2026',
     '33333333-3333-3333-3333-333333333301',
     '11111111-1111-1111-1111-111111111103', '11111111-1111-1111-1111-111111111104',
     'T20I', 'INTERNATIONAL', 'National Stadium, Karachi', now() - interval '26 hours',
     'COMPLETED', 'Match 2', '171/4 (19.4)', '168/8 (20)',
     NULL, 'Australia won by 6 wickets', true),
    ('44444444-4444-4444-4444-444444444403', 'pakistan-vs-australia-asia-cup-2026',
     '33333333-3333-3333-3333-333333333301',
     '11111111-1111-1111-1111-111111111101', '11111111-1111-1111-1111-111111111103',
     'T20I', 'INTERNATIONAL', 'Gaddafi Stadium, Lahore', now() + interval '30 hours',
     'SCHEDULED', 'Match 5', NULL, NULL, 'Day/night fixture, gates open two hours before the start.', NULL, true)
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- scorecard for the completed match
INSERT INTO cricket_innings (id, match_id, innings_number, batting_team_id, runs, wickets, overs_text, declared)
VALUES
    ('55555555-5555-5555-5555-555555555501', '44444444-4444-4444-4444-444444444402', 1,
     '11111111-1111-1111-1111-111111111104', 168, 8, '20', false),
    ('55555555-5555-5555-5555-555555555502', '44444444-4444-4444-4444-444444444402', 2,
     '11111111-1111-1111-1111-111111111103', 171, 4, '19.4', false)
ON CONFLICT DO NOTHING;

INSERT INTO cricket_batting_entry (id, innings_id, position, batter, dismissal, runs, balls, fours, sixes, not_out)
VALUES
    ('66666666-6666-6666-6666-666666666601', '55555555-5555-5555-5555-555555555501', 1, 'Phil Salt', 'c Head b Starc', 34, 21, 4, 2, false),
    ('66666666-6666-6666-6666-666666666602', '55555555-5555-5555-5555-555555555501', 2, 'Jos Buttler', 'b Cummins', 48, 33, 5, 2, false),
    ('66666666-6666-6666-6666-666666666603', '55555555-5555-5555-5555-555555555501', 3, 'Joe Root', 'run out (Maxwell)', 27, 24, 2, 0, false),
    ('66666666-6666-6666-6666-666666666604', '55555555-5555-5555-5555-555555555501', 4, 'Ben Stokes', 'not out', 41, 28, 3, 2, true),
    ('66666666-6666-6666-6666-666666666605', '55555555-5555-5555-5555-555555555501', 5, 'Liam Livingstone', 'c Warner b Zampa', 9, 8, 1, 0, false),
    ('66666666-6666-6666-6666-666666666606', '55555555-5555-5555-5555-555555555502', 1, 'Travis Head', 'c Buttler b Archer', 62, 38, 7, 2, false),
    ('66666666-6666-6666-6666-666666666607', '55555555-5555-5555-5555-555555555502', 2, 'David Warner', 'lbw b Rashid', 33, 26, 4, 0, false),
    ('66666666-6666-6666-6666-666666666608', '55555555-5555-5555-5555-555555555502', 3, 'Mitchell Marsh', 'not out', 44, 31, 3, 2, true),
    ('66666666-6666-6666-6666-666666666609', '55555555-5555-5555-5555-555555555502', 4, 'Glenn Maxwell', 'c Root b Stokes', 18, 12, 2, 1, false),
    ('66666666-6666-6666-6666-666666666610', '55555555-5555-5555-5555-555555555502', 5, 'Marcus Stoinis', 'not out', 9, 9, 1, 0, true)
ON CONFLICT DO NOTHING;

INSERT INTO cricket_bowling_entry (id, innings_id, position, bowler, overs_text, maidens, runs_conceded, wickets)
VALUES
    ('77777777-7777-7777-7777-777777777701', '55555555-5555-5555-5555-555555555501', 1, 'Mitchell Starc', '4', 0, 32, 2),
    ('77777777-7777-7777-7777-777777777702', '55555555-5555-5555-5555-555555555501', 2, 'Pat Cummins', '4', 0, 28, 2),
    ('77777777-7777-7777-7777-777777777703', '55555555-5555-5555-5555-555555555501', 3, 'Adam Zampa', '4', 0, 31, 1),
    ('77777777-7777-7777-7777-777777777704', '55555555-5555-5555-5555-555555555501', 4, 'Glenn Maxwell', '4', 0, 35, 1),
    ('77777777-7777-7777-7777-777777777705', '55555555-5555-5555-5555-555555555502', 1, 'Jofra Archer', '4', 0, 29, 2),
    ('77777777-7777-7777-7777-777777777706', '55555555-5555-5555-5555-555555555502', 2, 'Adil Rashid', '4', 0, 33, 1),
    ('77777777-7777-7777-7777-777777777707', '55555555-5555-5555-5555-555555555502', 3, 'Ben Stokes', '4', 0, 38, 1),
    ('77777777-7777-7777-7777-777777777708', '55555555-5555-5555-5555-555555555502', 4, 'Sam Curran', '3.4', 0, 30, 0)
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- commentary (completed + live match)
INSERT INTO cricket_commentary (id, match_id, over_text, kind, text)
VALUES
    ('88888888-8888-8888-8888-888888888801', '44444444-4444-4444-4444-444444444402', '19.4', 'FOUR',
     'Head finishes it in style, drilled through extra cover for four. Australia win by 6 wickets.'),
    ('88888888-8888-8888-8888-888888888802', '44444444-4444-4444-4444-444444444402', '18.2', 'WICKET',
     'Maxwell goes for the big one and finds deep midwicket. 24 needed from 10.'),
    ('88888888-8888-8888-8888-888888888803', '44444444-4444-4444-4444-444444444402', '15.5', 'SIX',
     'Marsh clears the ropes at long-on, 78 metres. Momentum shifting.'),
    ('88888888-8888-8888-8888-888888888804', '44444444-4444-4444-4444-444444444401', '18.2', 'WICKET',
     'Bumrah nails the yorker, Rizwan is bowled for 74. Pakistan lose their fourth.'),
    ('88888888-8888-8888-8888-888888888805', '44444444-4444-4444-4444-444444444401', '17.6', 'SIX',
     'Babar dances down and lofts the spinner over long-off. 150 up for Pakistan.'),
    ('88888888-8888-8888-8888-888888888806', '44444444-4444-4444-4444-444444444401', '16.3', 'FOUR',
     'Punched off the back foot through the covers, four more for Rizwan.')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- news
INSERT INTO news_category (id, slug, name, description, sort_order)
VALUES
    ('99999999-9999-9999-9999-999999999901', 'cricket', 'Cricket', 'Match reports, series news and analysis.', 1),
    ('99999999-9999-9999-9999-999999999902', 'analysis', 'Analysis', 'Long reads and tactical breakdowns.', 2)
ON CONFLICT DO NOTHING;

INSERT INTO news_article (id, slug, title, subtitle, summary, body, body_format, status, category_id,
                          author_display_name, cover_image_url, meta_title, meta_description, canonical_path,
                          keywords, featured, published_at, robots)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa01', 'pakistan-edge-india-in-asia-cup-thriller',
     'Pakistan edge India in an Asia Cup thriller',
     'Rizwan''s 74 sets up a last-over finish in Lahore',
     'Pakistan chased down 187 with two balls to spare in front of a packed Gaddafi Stadium.',
     '<p>Pakistan opened their Asia Cup campaign with a two-wicket win over India in Lahore.</p><p>Mohammad Rizwan top-scored with 74, while Babar Azam''s 46 kept the chase on course before a late flurry from the lower order.</p><p>India''s Jasprit Bumrah finished with three wickets, but the visitors could not defend 187.</p>',
     'HTML', 'PUBLISHED', '99999999-9999-9999-9999-999999999901', 'Demo Reporter',
     'https://images.unsplash.com/photo-1531415074968-036ba1b575da?w=1200',
     'Pakistan edge India in an Asia Cup thriller',
     'Rizwan''s 74 sets up a last-over finish in Lahore as Pakistan beat India by two wickets.',
     '/news/pakistan-edge-india-in-asia-cup-thriller',
     'Pakistan,India,Asia Cup,T20I', true, now() - interval '3 hours', 'index,follow'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa02', 'australia-hold-nerve-against-england',
     'Australia hold their nerve against England',
     'Head''s 62 makes the difference at the National Stadium',
     'England posted 168 for 8 but Australia got home with two balls to spare.',
     '<p>Travis Head''s 62 from 38 balls anchored Australia''s chase of 169 in Karachi.</p><p>England''s middle order never quite found top gear on a two-paced surface.</p>',
     'HTML', 'PUBLISHED', '99999999-9999-9999-9999-999999999901', 'Demo Reporter',
     'https://images.unsplash.com/photo-1593341646782-e0b495cff86d?w=1200',
     'Australia hold their nerve against England',
     'Australia chase 169 with two balls to spare in Karachi.',
     '/news/australia-hold-nerve-against-england',
     'Australia,England,Asia Cup', false, now() - interval '22 hours', 'index,follow'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa03', 'asia-cup-2026-what-to-watch',
     'Asia Cup 2026: what to watch in the group stage',
     'Four teams, six group games, one final in Lahore',
     'A short guide to the fixtures, the venues and the players to follow.',
     '<p>The group stage runs until 24 November, with the final scheduled for 30 November in Lahore.</p><p>All four sides play each other once before the top two meet in the final.</p>',
     'HTML', 'PUBLISHED', '99999999-9999-9999-9999-999999999902', 'Demo Editor', NULL,
     'Asia Cup 2026 guide', 'Fixtures, venues and players to watch in the Asia Cup 2026 group stage.',
     '/news/asia-cup-2026-what-to-watch', 'Asia Cup,fixtures,preview', false, now() - interval '2 days', 'index,follow'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa04', 'how-pakistan-rebuilt-their-middle-order',
     'How Pakistan rebuilt their middle order',
     'From collapse-prone to calm: a look at the numbers',
     'Pakistan have lost fewer middle-order wickets per innings than any side in the tournament.',
     '<p>Since the last World Cup Pakistan have changed their approach between overs 7 and 15.</p><p>The result is a middle order that keeps the run rate moving without losing wickets in clusters.</p>',
     'HTML', 'PUBLISHED', '99999999-9999-9999-9999-999999999902', 'Demo Editor', NULL,
     'Pakistan middle order analysis', 'Why Pakistan''s middle order has become the calmest in the tournament.',
     '/news/how-pakistan-rebuilt-their-middle-order', 'Pakistan,analysis,batting', false, now() - interval '3 days', 'index,follow'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa05', 'the-return-of-the-yorker',
     'The return of the yorker',
     'Death bowling is back to being a specialist''s job',
     'Across the tournament, yorkers have accounted for a fifth of all death-over wickets.',
     '<p>Two new balls and bigger boundaries have pushed captains back towards genuine death specialists.</p><p>Bumrah, Afridi and Starc all bowl more than 60 per cent of their final-over deliveries as yorkers.</p>',
     'HTML', 'PUBLISHED', '99999999-9999-9999-9999-999999999902', 'Demo Analyst', NULL,
     'Death bowling analysis', 'Why the yorker is back at the centre of death bowling.',
     '/news/the-return-of-the-yorker', 'bowling,yorker,analysis', false, now() - interval '5 days', 'index,follow')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- videos
INSERT INTO video (id, slug, title, description, category, thumbnail_url, video_url, duration_text, published_at, visible)
VALUES
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb01', 'asia-cup-2026-highlights',
     'Asia Cup 2026: opening night highlights',
     'The best of Pakistan v India in Lahore.',
     'Highlights', 'https://images.unsplash.com/photo-1531415074968-036ba1b575da?w=800',
     'https://www.youtube.com/watch?v=dQw4w9WgXcQ', '06:12', now() - interval '4 hours', true),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb02', 'tactics-board-middle-overs',
     'Tactics board: building through the middle overs',
     'A three-minute breakdown of strike rotation.',
     'Analysis', 'https://images.unsplash.com/photo-1593341646782-e0b495cff86d?w=800',
     'https://www.youtube.com/watch?v=dQw4w9WgXcQ', '03:45', now() - interval '2 days', true),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb03', 'inside-the-dressing-room',
     'Inside the dressing room: Karachi',
     'Access-all-areas from match two.',
     'Behind the scenes', 'https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?w=800',
     'https://www.youtube.com/watch?v=dQw4w9WgXcQ', '08:30', now() - interval '4 days', true)
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- homepage layout
INSERT INTO homepage_section (id, section_key, type, title, sort_order, enabled, config_json)
VALUES
    ('cccccccc-cccc-cccc-cccc-cccccccccc01', 'hero', 'HERO', 'Top story', 1, true,
     '{"limit": 1, "featured": true}'),
    ('cccccccc-cccc-cccc-cccc-cccccccccc02', 'headlines', 'HEADLINE_LIST', 'Latest news', 2, true,
     '{"limit": 6}'),
    ('cccccccc-cccc-cccc-cccc-cccccccccc03', 'live-matches', 'MATCH_STRIP', 'Live & upcoming', 3, true,
     '{"limit": 4, "statuses": ["LIVE", "SCHEDULED"]}'),
    ('cccccccc-cccc-cccc-cccc-cccccccccc04', 'points', 'POINTS_TABLE', 'Asia Cup standings', 4, true,
     '{"series": "asia-cup-2026"}'),
    ('cccccccc-cccc-cccc-cccc-cccccccccc05', 'videos', 'VIDEO_RAIL', 'Watch', 5, true,
     '{"limit": 4}')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- one legacy URL (SEO demo)
INSERT INTO seo_redirect (id, from_path, to_path, status_code, note, enabled)
VALUES
    ('dddddddd-dddd-dddd-dddd-dddddddddd01', '/cricket/live', '/cricket/live-scores', 301,
     'Old live-score URL kept alive for search engines.', true)
ON CONFLICT DO NOTHING;


-- Additional demo content, applied after R__demo_data.sql (repeatable migrations run in name order).
-- Same rules: opt-in folder, fixed UUIDs, ON CONFLICT DO NOTHING, no user accounts, DML only.

-- ---------------------------------------------------------------- four more teams
INSERT INTO cricket_team (id, slug, name, short_name, category, country_code, flag_emoji, sport, description)
VALUES
    ('11111111-1111-1111-1111-111111111105', 'new-zealand', 'New Zealand', 'NZ', 'INTERNATIONAL', 'NZ', '🇳🇿', 'cricket',
     'New Zealand national cricket team, all formats.'),
    ('11111111-1111-1111-1111-111111111106', 'south-africa', 'South Africa', 'SA', 'INTERNATIONAL', 'ZA', '🇿🇦', 'cricket',
     'South Africa national cricket team, all formats.'),
    ('11111111-1111-1111-1111-111111111107', 'sri-lanka', 'Sri Lanka', 'SL', 'INTERNATIONAL', 'LK', '🇱🇰', 'cricket',
     'Sri Lanka national cricket team, all formats.'),
    ('11111111-1111-1111-1111-111111111108', 'bangladesh', 'Bangladesh', 'BAN', 'INTERNATIONAL', 'BD', '🇧🇩', 'cricket',
     'Bangladesh national cricket team, all formats.')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- twelve more players
INSERT INTO cricket_player (id, slug, full_name, team_id, country, role, batting_style, bowling_style, description)
VALUES
    ('22222222-2222-2222-2222-222222222213', 'kane-williamson', 'Kane Williamson',
     '11111111-1111-1111-1111-111111111105', 'New Zealand', 'Batter', 'Right-hand bat', 'Right-arm offbreak',
     'New Zealand top-order batter.'),
    ('22222222-2222-2222-2222-222222222214', 'devon-conway', 'Devon Conway',
     '11111111-1111-1111-1111-111111111105', 'New Zealand', 'Batter', 'Left-hand bat', NULL,
     'New Zealand opener.'),
    ('22222222-2222-2222-2222-222222222215', 'trent-boult', 'Trent Boult',
     '11111111-1111-1111-1111-111111111105', 'New Zealand', 'Bowler', 'Right-hand bat', 'Left-arm fast-medium',
     'New Zealand new-ball bowler.'),
    ('22222222-2222-2222-2222-222222222216', 'temba-bavuma', 'Temba Bavuma',
     '11111111-1111-1111-1111-111111111106', 'South Africa', 'Batter', 'Right-hand bat', NULL,
     'South Africa captain and top-order batter.'),
    ('22222222-2222-2222-2222-222222222217', 'quinton-de-kock', 'Quinton de Kock',
     '11111111-1111-1111-1111-111111111106', 'South Africa', 'Wicketkeeper Batter', 'Left-hand bat', NULL,
     'South Africa wicketkeeper and opener.'),
    ('22222222-2222-2222-2222-222222222218', 'kagiso-rabada', 'Kagiso Rabada',
     '11111111-1111-1111-1111-111111111106', 'South Africa', 'Bowler', 'Right-hand bat', 'Right-arm fast',
     'South Africa strike bowler.'),
    ('22222222-2222-2222-2222-222222222219', 'kusal-mendis', 'Kusal Mendis',
     '11111111-1111-1111-1111-111111111107', 'Sri Lanka', 'Wicketkeeper Batter', 'Right-hand bat', NULL,
     'Sri Lanka wicketkeeper and top-order batter.'),
    ('22222222-2222-2222-2222-222222222220', 'pathum-nissanka', 'Pathum Nissanka',
     '11111111-1111-1111-1111-111111111107', 'Sri Lanka', 'Batter', 'Right-hand bat', NULL,
     'Sri Lanka opener.'),
    ('22222222-2222-2222-2222-222222222221', 'wanindu-hasaranga', 'Wanindu Hasaranga',
     '11111111-1111-1111-1111-111111111107', 'Sri Lanka', 'All-rounder', 'Right-hand bat', 'Right-arm legbreak',
     'Sri Lanka leg-spinning all-rounder.'),
    ('22222222-2222-2222-2222-222222222222', 'shakib-al-hasan', 'Shakib Al Hasan',
     '11111111-1111-1111-1111-111111111108', 'Bangladesh', 'All-rounder', 'Left-hand bat', 'Left-arm orthodox',
     'Bangladesh all-rounder.'),
    ('22222222-2222-2222-2222-222222222223', 'litton-das', 'Litton Das',
     '11111111-1111-1111-1111-111111111108', 'Bangladesh', 'Wicketkeeper Batter', 'Right-hand bat', NULL,
     'Bangladesh wicketkeeper and opener.'),
    ('22222222-2222-2222-2222-222222222224', 'mustafizur-rahman', 'Mustafizur Rahman',
     '11111111-1111-1111-1111-111111111108', 'Bangladesh', 'Bowler', 'Left-hand bat', 'Left-arm fast-medium',
     'Bangladesh cutter specialist.')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- second series + standings
INSERT INTO cricket_tournament (id, slug, name, short_name, description, format, category, season, host,
                                dates_text, start_date, end_date, status)
VALUES
    ('33333333-3333-3333-3333-333333333302', 'tri-nation-t20i-series-2026', 'Tri-Nation T20I Series 2026',
     'Tri-Nation Series',
     'New Zealand, South Africa and Sri Lanka warm up for the Asia Cup knockout stage.',
     'T20I', 'INTERNATIONAL', '2026', 'Sri Lanka',
     '10 Nov – 20 Nov 2026', CURRENT_DATE - 6, CURRENT_DATE + 4, 'ONGOING')
ON CONFLICT DO NOTHING;

INSERT INTO cricket_tournament_team (tournament_id, team_id)
VALUES
    ('33333333-3333-3333-3333-333333333302', '11111111-1111-1111-1111-111111111105'),
    ('33333333-3333-3333-3333-333333333302', '11111111-1111-1111-1111-111111111106'),
    ('33333333-3333-3333-3333-333333333302', '11111111-1111-1111-1111-111111111107')
ON CONFLICT DO NOTHING;

INSERT INTO cricket_standing (id, tournament_id, team_id, position, played, won, lost, tied, no_result, points, nrr)
VALUES
    ('33333333-3333-3333-3333-333333333321', '33333333-3333-3333-3333-333333333302', '11111111-1111-1111-1111-111111111106', 1, 2, 2, 0, 0, 0, 4, '+0.980'),
    ('33333333-3333-3333-3333-333333333322', '33333333-3333-3333-3333-333333333302', '11111111-1111-1111-1111-111111111105', 2, 2, 1, 1, 0, 0, 2, '+0.115'),
    ('33333333-3333-3333-3333-333333333323', '33333333-3333-3333-3333-333333333302', '11111111-1111-1111-1111-111111111107', 3, 2, 0, 2, 0, 0, 0, '-1.204')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- five more fixtures
INSERT INTO cricket_match (id, slug, tournament_id, team1_id, team2_id, format, category, venue, start_time,
                           status, match_number, team1_score, team2_score, note, result_text, visible)
VALUES
    ('44444444-4444-4444-4444-444444444404', 'new-zealand-vs-south-africa-tri-series-2026',
     '33333333-3333-3333-3333-333333333302',
     '11111111-1111-1111-1111-111111111105', '11111111-1111-1111-1111-111111111106',
     'T20I', 'INTERNATIONAL', 'R. Premadasa Stadium, Colombo', now() - interval '1 hour',
     'LIVE', 'Match 4', '142/3 (15.1)', '178/6 (20)', 'South Africa batted first after winning the toss.', NULL, true),
    ('44444444-4444-4444-4444-444444444405', 'sri-lanka-vs-bangladesh-tri-series-2026',
     '33333333-3333-3333-3333-333333333302',
     '11111111-1111-1111-1111-111111111107', '11111111-1111-1111-1111-111111111108',
     'T20I', 'INTERNATIONAL', 'Pallekele International Stadium', now() - interval '3 days',
     'COMPLETED', 'Match 2', '159/8 (20)', '155/9 (20)',
     NULL, 'Sri Lanka won by 4 runs', true),
    ('44444444-4444-4444-4444-444444444406', 'pakistan-vs-new-zealand-asia-cup-2026',
     '33333333-3333-3333-3333-333333333301',
     '11111111-1111-1111-1111-111111111101', '11111111-1111-1111-1111-111111111105',
     'T20I', 'INTERNATIONAL', 'Gaddafi Stadium, Lahore', now() + interval '2 days',
     'SCHEDULED', 'Match 7', NULL, NULL, 'Winner tops the group; reserve day reserved for Sunday.', NULL, true),
    ('44444444-4444-4444-4444-444444444407', 'india-vs-south-africa-asia-cup-2026',
     '33333333-3333-3333-3333-333333333301',
     '11111111-1111-1111-1111-111111111102', '11111111-1111-1111-1111-111111111106',
     'T20I', 'INTERNATIONAL', 'National Stadium, Karachi', now() + interval '4 days',
     'SCHEDULED', 'Match 9', NULL, NULL, 'Tickets on sale from Thursday.', NULL, true),
    ('44444444-4444-4444-4444-444444444408', 'australia-vs-sri-lanka-asia-cup-2026',
     '33333333-3333-3333-3333-333333333301',
     '11111111-1111-1111-1111-111111111103', '11111111-1111-1111-1111-111111111107',
     'T20I', 'INTERNATIONAL', 'Gaddafi Stadium, Lahore', now() - interval '5 days',
     'ABANDONED', 'Match 1', '36/1 (4.2)', NULL, 'Persistent rain after the fourth over.', 'No result — rain', true)
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- scorecard for Sri Lanka v Bangladesh
INSERT INTO cricket_innings (id, match_id, innings_number, batting_team_id, runs, wickets, overs_text, declared)
VALUES
    ('55555555-5555-5555-5555-555555555503', '44444444-4444-4444-4444-444444444405', 1,
     '11111111-1111-1111-1111-111111111107', 159, 8, '20', false),
    ('55555555-5555-5555-5555-555555555504', '44444444-4444-4444-4444-444444444405', 2,
     '11111111-1111-1111-1111-111111111108', 155, 9, '20', false)
ON CONFLICT DO NOTHING;

INSERT INTO cricket_batting_entry (id, innings_id, position, batter, dismissal, runs, balls, fours, sixes, not_out)
VALUES
    ('66666666-6666-6666-6666-666666666611', '55555555-5555-5555-5555-555555555503', 1, 'Pathum Nissanka', 'c Litton b Mustafizur', 52, 34, 6, 2, false),
    ('66666666-6666-6666-6666-666666666612', '55555555-5555-5555-5555-555555555503', 2, 'Kusal Mendis', 'b Shakib', 31, 24, 4, 0, false),
    ('66666666-6666-6666-6666-666666666613', '55555555-5555-5555-5555-555555555503', 3, 'Wanindu Hasaranga', 'not out', 38, 22, 2, 3, true),
    ('66666666-6666-6666-6666-666666666614', '55555555-5555-5555-5555-555555555503', 4, 'Dasun Shanaka', 'run out', 12, 11, 1, 0, false),
    ('66666666-6666-6666-6666-666666666615', '55555555-5555-5555-5555-555555555504', 1, 'Litton Das', 'c Mendis b Hasaranga', 44, 31, 5, 1, false),
    ('66666666-6666-6666-6666-666666666616', '55555555-5555-5555-5555-555555555504', 2, 'Shakib Al Hasan', 'not out', 57, 41, 4, 2, true),
    ('66666666-6666-6666-6666-666666666617', '55555555-5555-5555-5555-555555555504', 3, 'Towhid Hridoy', 'lbw b Hasaranga', 18, 16, 2, 0, false),
    ('66666666-6666-6666-6666-666666666618', '55555555-5555-5555-5555-555555555504', 4, 'Mustafizur Rahman', 'not out', 3, 5, 0, 0, true)
ON CONFLICT DO NOTHING;

INSERT INTO cricket_bowling_entry (id, innings_id, position, bowler, overs_text, maidens, runs_conceded, wickets)
VALUES
    ('77777777-7777-7777-7777-777777777709', '55555555-5555-5555-5555-555555555503', 1, 'Mustafizur Rahman', '4', 0, 27, 2),
    ('77777777-7777-7777-7777-777777777710', '55555555-5555-5555-5555-555555555503', 2, 'Shakib Al Hasan', '4', 1, 24, 2),
    ('77777777-7777-7777-7777-777777777711', '55555555-5555-5555-5555-555555555503', 3, 'Taskin Ahmed', '4', 0, 41, 1),
    ('77777777-7777-7777-7777-777777777712', '55555555-5555-5555-5555-555555555504', 1, 'Wanindu Hasaranga', '4', 0, 26, 3),
    ('77777777-7777-7777-7777-777777777713', '55555555-5555-5555-5555-555555555504', 2, 'Maheesh Theekshana', '4', 0, 30, 2),
    ('77777777-7777-7777-7777-777777777714', '55555555-5555-5555-5555-555555555504', 3, 'Dasun Shanaka', '4', 0, 36, 1)
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- commentary
INSERT INTO cricket_commentary (id, match_id, over_text, kind, text)
VALUES
    ('88888888-8888-8888-8888-888888888807', '44444444-4444-4444-4444-444444444405', '19.6', 'WICKET',
     'Hasaranga closes it out — Bangladesh fall four short. Sri Lanka win by 4 runs.'),
    ('88888888-8888-8888-8888-888888888808', '44444444-4444-4444-4444-444444444405', '18.4', 'SIX',
     'Shakib keeps Bangladesh alive, slog-swept over deep midwicket.'),
    ('88888888-8888-8888-8888-888888888809', '44444444-4444-4444-4444-444444444405', '16.1', 'WICKET',
     'Hridoy trapped in front, Hasaranga has his third.'),
    ('88888888-8888-8888-8888-888888888810', '44444444-4444-4444-4444-444444444404', '15.1', 'FOUR',
     'Conway threads the gap at cover — New Zealand need 37 from 29.'),
    ('88888888-8888-8888-8888-888888888811', '44444444-4444-4444-4444-444444444404', '14.3', 'WICKET',
     'Rabada removes Williamson with a full one; the chase wobbles.'),
    ('88888888-8888-8888-8888-888888888812', '44444444-4444-4444-4444-444444444408', '4.2', 'INFO',
     'Rain gets heavier and the umpires call the players off. No result.')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- seven more articles
INSERT INTO news_article (id, slug, title, subtitle, summary, body, body_format, status, category_id,
                          author_display_name, cover_image_url, meta_title, meta_description, canonical_path,
                          keywords, featured, published_at, robots)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa06', 'new-zealand-seal-tri-series-with-clean-sweep',
     'New Zealand seal the tri-series with a clean sweep',
     'Boult takes four as South Africa fold for 121',
     'New Zealand finished the group stage unbeaten after a 34-run win in Colombo.',
     '<p>Trent Boult''s four-wicket haul set up a comfortable win as New Zealand completed a 3-0 sweep.</p><p>South Africa''s top order never came to terms with the moving ball under lights.</p>',
     'HTML', 'PUBLISHED', '99999999-9999-9999-9999-999999999901', 'Demo Reporter',
     'https://images.unsplash.com/photo-1531415074968-036ba1b575da?w=1200',
     'New Zealand seal the tri-series with a clean sweep',
     'Trent Boult takes four as New Zealand complete an unbeaten group stage.',
     '/news/new-zealand-seal-tri-series-with-clean-sweep',
     'New Zealand,South Africa,tri-series', false, now() - interval '6 hours', 'index,follow'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa07', 'south-africa-name-squad-for-knockouts',
     'South Africa name their squad for the knockout stage',
     'Two changes from the group-stage fifteen',
     'A fit-again seamer returns as South Africa reshuffle their attack.',
     '<p>South Africa have recalled a second specialist seamer for the semi-finals.</p><p>The squad stays at fifteen, with one batter released to domestic duty.</p>',
     'HTML', 'PUBLISHED', '99999999-9999-9999-9999-999999999901', 'Demo Reporter', NULL,
     'South Africa knockout squad', 'South Africa make two changes to their fifteen for the knockouts.',
     '/news/south-africa-name-squad-for-knockouts',
     'South Africa,squad,knockouts', false, now() - interval '11 hours', 'index,follow'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa08', 'sri-lanka-turn-to-youth-for-tri-series',
     'Sri Lanka turn to youth for the tri-series',
     'Three debutants in the playing eleven',
     'Sri Lanka handed caps to three under-23 players in Pallekele.',
     '<p>Sri Lanka''s selectors backed a younger core for the tri-series, and the returns were immediate.</p>',
     'HTML', 'PUBLISHED', '99999999-9999-9999-9999-999999999901', 'Demo Reporter', NULL,
     'Sri Lanka youth tri-series', 'Three debutants feature as Sri Lanka rebuild for the next cycle.',
     '/news/sri-lanka-turn-to-youth-for-tri-series',
     'Sri Lanka,youth,debutants', false, now() - interval '1 day', 'index,follow'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa09', 'bangladesh-eye-first-away-win-in-three-years',
     'Bangladesh eye a first away win in three years',
     'Four runs short in Pallekele, but the signs are there',
     'Bangladesh pushed Sri Lanka to the last ball in a four-run defeat.',
     '<p>Chasing 160, Bangladesh needed five from the final over and fell four runs short.</p><p>The margin was small; the improvement was not.</p>',
     'HTML', 'PUBLISHED', '99999999-9999-9999-9999-999999999901', 'Demo Reporter', NULL,
     'Bangladesh away win', 'A four-run defeat in Pallekele still marks Bangladesh''s best away display in years.',
     '/news/bangladesh-eye-first-away-win-in-three-years',
     'Bangladesh,Sri Lanka,analysis', false, now() - interval '2 days', 'index,follow'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa0a', 'five-bowlers-who-could-decide-the-final',
     'Five bowlers who could decide the final',
     'Death overs, yorkers and one very good googly',
     'A shortlist of the bowlers most likely to swing the knockout games.',
     '<p>From Bumrah''s yorker to Hasaranga''s googly, these are the five spells to watch.</p>',
     'HTML', 'PUBLISHED', '99999999-9999-9999-9999-999999999902', 'Demo Analyst', NULL,
     'Bowlers who could decide the final', 'Five bowlers most likely to swing the knockout stage.',
     '/news/five-bowlers-who-could-decide-the-final',
     'bowling,analysis,final', false, now() - interval '3 days', 'index,follow'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa0b', 'how-the-points-table-can-still-change',
     'How the points table can still change',
     'Net run rate, not points, separates three teams',
     'With two games left, three sides are level on points.',
     '<p>Net run rate could decide who joins Pakistan in the semi-finals.</p>',
     'HTML', 'PUBLISHED', '99999999-9999-9999-9999-999999999902', 'Demo Editor', NULL,
     'Points table scenarios', 'Three teams level on points going into the final group games.',
     '/news/how-the-points-table-can-still-change',
     'points table,net run rate,analysis', false, now() - interval '4 days', 'index,follow'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa0c', 'weather-forecast-for-final-week-in-lahore',
     'Weather forecast for final week in Lahore',
     'A dry spell expected from Thursday',
     'Clear skies are forecast for the semi-finals and the final.',
     '<p>The forecast suggests no rain interruptions from Thursday onwards in Lahore.</p>',
     'HTML', 'PUBLISHED', '99999999-9999-9999-9999-999999999902', 'Demo Editor', NULL,
     'Lahore weather forecast', 'Clear skies forecast for the Asia Cup semi-finals and final.',
     '/news/weather-forecast-for-final-week-in-lahore',
     'weather,Lahore,schedule', false, now() - interval '5 days', 'noindex,follow')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- three more videos
INSERT INTO video (id, slug, title, description, category, thumbnail_url, video_url, duration_text, published_at, visible)
VALUES
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb04', 'top-catches-of-the-tournament',
     'Top catches of the tournament so far',
     'Six of the best from the group stage.',
     'Highlights', 'https://images.unsplash.com/photo-1531415074968-036ba1b575da?w=800',
     'https://www.youtube.com/watch?v=dQw4w9WgXcQ', '04:58', now() - interval '1 day', true),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb05', 'pitch-report-lahore-day-night',
     'Pitch report: Lahore, day/night',
     'What the surface is doing under lights.',
     'Analysis', 'https://images.unsplash.com/photo-1593341646782-e0b495cff86d?w=800',
     'https://www.youtube.com/watch?v=dQw4w9WgXcQ', '02:31', now() - interval '3 days', true),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb06', 'fan-cam-final-week',
     'Fan cam: the final week',
     'Colombo, Karachi and Lahore, from the stands.',
     'Behind the scenes', 'https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?w=800',
     'https://www.youtube.com/watch?v=dQw4w9WgXcQ', '07:14', now() - interval '5 days', true)
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- one more homepage block
INSERT INTO homepage_section (id, section_key, type, title, sort_order, enabled, config_json)
VALUES
    ('cccccccc-cccc-cccc-cccc-cccccccccc06', 'editor-picks', 'CUSTOM', 'Editor''s picks', 6, true,
     '{"limit": 3, "slugs": ["five-bowlers-who-could-decide-the-final", "how-the-points-table-can-still-change"]}')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- two more legacy URLs
INSERT INTO seo_redirect (id, from_path, to_path, status_code, note, enabled)
VALUES
    ('dddddddd-dddd-dddd-dddd-dddddddddd02', '/series', '/cricket/series', 301,
     'Old series index kept for search engines.', true),
    ('dddddddd-dddd-dddd-dddd-dddddddddd03', '/match-centre', '/cricket/live-scores', 302,
     'Temporary redirect while the match centre moves.', true)
ON CONFLICT DO NOTHING;
