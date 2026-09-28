-- ✅ ENUM ------------------------------------------------------------

-- 핑퐁 상태 (답변 대기 / 답변 완료)
CREATE TYPE ping_pong_status AS ENUM ('PENDING', 'ANSWERED');

-- 콘텐츠 신고 대상 유형
CREATE TYPE content_report_type AS ENUM ('PING_PONG_QUESTION', 'PING_PONG_ANSWER');

-- ✅ 테이블 ------------------------------------------------------------

-- 핑퐁 테이블 (1질문-1답변 구조, 답변자는 게시물(user_keyword) 작성자로 한정)
CREATE TABLE ping_pong (
    id              BIGSERIAL PRIMARY KEY,
    user_keyword_id BIGINT           NOT NULL,
    questioner_id   BIGINT           NOT NULL,
    question        VARCHAR(2200)    NOT NULL,
    answer          VARCHAR(2200),
    status          ping_pong_status NOT NULL DEFAULT 'PENDING',
    answered_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ping_pong_user_keyword FOREIGN KEY (user_keyword_id) REFERENCES user_keyword(id) ON DELETE CASCADE,
    CONSTRAINT fk_ping_pong_questioner FOREIGN KEY (questioner_id) REFERENCES "user"(id) ON DELETE CASCADE,
    -- 답변 내용과 답변 시각은 항상 함께 존재하거나 함께 없어야 한다.
    CONSTRAINT chk_ping_pong_answer_consistency CHECK (
        (answer IS NULL AND answered_at IS NULL) OR (answer IS NOT NULL AND answered_at IS NOT NULL)
    )
);

-- 콘텐츠 신고 테이블 (신고 시점의 콘텐츠를 스냅샷으로 보관, 원본 삭제 후에도 유지)
CREATE TABLE content_report (
    id               BIGSERIAL PRIMARY KEY,
    reporter_id      BIGINT              NOT NULL,
    reported_user_id BIGINT              NOT NULL,
    content_type     content_report_type NOT NULL,
    content_id       BIGINT              NOT NULL, -- 다형성 참조이므로 FK 없음
    content_snapshot TEXT                NOT NULL,
    reason_id        BIGINT              NOT NULL,
    custom_reason    TEXT,
    created_at       TIMESTAMPTZ         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMPTZ         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_content_report_reporter FOREIGN KEY (reporter_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_content_report_reported_user FOREIGN KEY (reported_user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_content_report_reason FOREIGN KEY (reason_id) REFERENCES report_reason(id) ON DELETE RESTRICT,
    -- 한 사람이 한 콘텐츠에 대해 한 번만 신고 가능
    CONSTRAINT uq_content_report_reporter_content UNIQUE (reporter_id, content_type, content_id),
    CONSTRAINT chk_content_report_not_self CHECK (reporter_id <> reported_user_id)
);

-- ✅ 인덱스 ------------------------------------------------------------

-- 게시물별 핑퐁 목록 조회 (최신순)
CREATE INDEX idx_ping_pong_user_keyword_created ON ping_pong (user_keyword_id, created_at, id);

-- 동일 사용자의 동일 게시물 연속 작성 제한 확인
CREATE INDEX idx_ping_pong_questioner_keyword_created ON ping_pong (questioner_id, user_keyword_id, created_at);

-- 콘텐츠별 신고 집계 (노출 제한 판단)
CREATE INDEX idx_content_report_content ON content_report (content_type, content_id);

-- 피신고자 기준 조회 및 계정 삭제 시 CASCADE 처리
CREATE INDEX idx_content_report_reported_user ON content_report (reported_user_id);
