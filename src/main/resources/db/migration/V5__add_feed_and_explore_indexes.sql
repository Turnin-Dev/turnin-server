CREATE INDEX IF NOT EXISTS idx_user_keyword_active_id
ON user_keyword (id DESC)
WHERE is_active = true;

CREATE INDEX IF NOT EXISTS idx_user_keyword_user_active_created
ON user_keyword (user_id, created_at DESC)
WHERE is_active = true;