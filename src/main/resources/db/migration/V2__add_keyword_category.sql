-- keyword_category ENUM 타입 추가
create type keyword_category as ENUM (
    'DAILY',
    'FOOD',
    'FASHION_BEAUTY',
    'TRAVEL',
    'FITNESS',
    'CULTURE',
    'MUSIC',
    'PET',
    'INTERIOR',
    'EMOTION',
    'ROMANCE',
    'CAREER',
    'FINANCE',
    'TECH',
    'GAMING',
    'HUMOR',
    'HOBBY',
    'VEHICLE',
    'SHOPPING',
    'ISSUE'
);

-- keyword 테이블에 카테고리 컬럼 추가
ALTER TABLE keyword
    ADD COLUMN category keyword_category,
    ADD COLUMN category_similarity DOUBLE PRECISION;

-- 키워드 카테고리 인덱스
CREATE INDEX idx_keyword_category ON keyword (category);

-- HNSW 인덱스 제거 (카테고리 방식으로 대체)
DROP INDEX IF EXISTS idx_keyword_embedding_hnsw;