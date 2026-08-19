DROP INDEX IF EXISTS idx_user_keyword_active_combo;

CREATE INDEX IF NOT EXISTS idx_user_keyword_active_combo
ON user_keyword (keyword_id)
INCLUDE (user_id, updated_at)
WHERE is_active = true;