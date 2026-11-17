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
