INSERT INTO users (nickname, created_at, updated_at) VALUES ('재현', NOW(), NOW());
INSERT INTO users (nickname, created_at, updated_at) VALUES ('영희', NOW(), NOW());
INSERT INTO users (nickname, created_at, updated_at) VALUES ('철수', NOW(), NOW());
INSERT INTO users (nickname, created_at, updated_at) VALUES ('민수', NOW(), NOW());
INSERT INTO users (nickname, created_at, updated_at) VALUES ('수진', NOW(), NOW());

INSERT INTO cafe_visits (name, address, lat, lng, has_outlet, wifi_speed, study_score, rating, price_level, memo, user_id, visited_at, created_at, updated_at, report_count, flagged_for_review)
VALUES ('스타벅스 강남점', '서울 강남구', 37.4979, 127.0276, true, 'FAST', 5, 4, 3, '콘센트 많음', 1, NOW(), NOW(), NOW(), 0, false);

INSERT INTO cafe_visits (name, address, lat, lng, has_outlet, wifi_speed, study_score, rating, price_level, memo, user_id, visited_at, created_at, updated_at, report_count, flagged_for_review)
VALUES ('투썸 홍대', '서울 마포구', 37.5563, 126.9236, true, 'NORMAL', 4, 4, 2, '조용함', 2, NOW(), NOW(), NOW(), 0, false);

INSERT INTO cafe_visits (name, address, lat, lng, has_outlet, wifi_speed, study_score, rating, price_level, memo, user_id, visited_at, created_at, updated_at, report_count, flagged_for_review)
VALUES ('이디야 신촌', '서울 서대문구', 37.5559, 126.9367, false, 'SLOW', 2, 3, 1, '와이파이 느림', 3, NOW(), NOW(), NOW(), 0, false);

INSERT INTO cafe_visits (name, address, lat, lng, has_outlet, wifi_speed, study_score, rating, price_level, memo, user_id, visited_at, created_at, updated_at, report_count, flagged_for_review)
VALUES ('블루보틀 성수', '서울 성동구', 37.5446, 127.0560, true, 'FAST', 5, 5, 4, '분위기 좋음', 4, NOW(), NOW(), NOW(), 0, false);

INSERT INTO cafe_visits (name, address, lat, lng, has_outlet, wifi_speed, study_score, rating, price_level, memo, user_id, visited_at, created_at, updated_at, report_count, flagged_for_review)
VALUES ('할리스 종각', '서울 종로구', 37.5703, 126.9826, true, 'NORMAL', 3, 3, 2, '평범', 5, NOW(), NOW(), NOW(), 0, false);