-- 키워드 카테고리 필드/ENUM 삭제
drop index IF EXISTS idx_keyword_category;

alter table keyword
    drop COLUMN IF EXISTS category,
    drop COLUMN IF EXISTS category_similarity;

drop type IF EXISTS keyword_category;