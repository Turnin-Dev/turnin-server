-- 중복되거나 불필요한 인덱스 제거
drop index IF EXISTS idx_user_keyword_combo_keyword;
drop index IF EXISTS idx_block_lookup;

-- 새로운 키워드 partial 인덱스 추가
create index IF NOT EXISTS idx_user_keyword_active_combo
ON user_keyword (keyword_id, user_id)
WHERE is_active = true;

-- 탐색 쿼리 롤백으로 인한 HNSW 인덱스 추가
create index IF NOT EXISTS idx_keyword_embedding_hnsw
ON keyword
USING hnsw (embedding vector_cosine_ops)
with (m = 16, ef_construction = 64);