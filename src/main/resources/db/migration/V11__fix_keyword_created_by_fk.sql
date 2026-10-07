-- 키워드 최초 등록자(created_by) FK를 코드 정의(Keywords: nullable, SET NULL)와 일치시킨다.
-- 기존 RESTRICT 제약 때문에 키워드를 처음 등록한 사용자는 계정 Hard Delete가 실패했다.
-- 키워드는 여러 사용자가 공유하므로, 등록자가 삭제되어도 키워드는 유지하고 등록자만 비운다.

ALTER TABLE keyword
    ALTER COLUMN created_by DROP NOT NULL;

ALTER TABLE keyword
    DROP CONSTRAINT fk_keyword_creator,
    ADD CONSTRAINT fk_keyword_creator
        FOREIGN KEY (created_by) REFERENCES "user"(id) ON DELETE SET NULL;
