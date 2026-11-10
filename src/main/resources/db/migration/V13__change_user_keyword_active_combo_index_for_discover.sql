-- 탐색 쿼리의 키워드별 보유자 상한(최근 활동 순 N명)을 인덱스 순서로 처리하기 위해 인덱스를 교체한다.
-- 기존: (keyword_id) INCLUDE (user_id, updated_at) → 키워드의 보유자를 전부 읽은 뒤 정렬해야 했다.
-- 변경: (keyword_id, updated_at DESC, user_id) → 키워드별로 최근 활동 순 N명만 읽고 멈춘다.
-- 주의: 인덱스를 다시 만드는 동안 user_keyword 쓰기가 잠시 막히므로, 사용자가 적은 시간에 배포한다.

DROP INDEX IF EXISTS idx_user_keyword_active_combo;

CREATE INDEX IF NOT EXISTS idx_user_keyword_active_combo
ON user_keyword (keyword_id, updated_at DESC, user_id)
WHERE is_active = true;
