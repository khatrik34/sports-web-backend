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
