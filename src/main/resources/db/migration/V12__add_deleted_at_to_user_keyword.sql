-- 작성자 쪽 사유(직접 삭제, 계정 탈퇴)로 삭제된 게시물의 삭제 시각
-- 신고 내역 때문에 삭제하지 못하고 비활성화로 남긴 게시물을, 운영자 숨김과 구분하고 1년 후 파기하기 위해 사용한다.
ALTER TABLE user_keyword
    ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE NULL;

-- 삭제된 게시물 파기 배치 조회용 (삭제된 게시물만 포함하는 부분 인덱스)
CREATE INDEX idx_user_keyword_deleted_at ON user_keyword (deleted_at) WHERE deleted_at IS NOT NULL;
