-- ✅ ENUM ------------------------------------------------------------

-- 콘텐츠 신고 대상 유형
CREATE TYPE content_report_type AS ENUM ('PING_PONG_QUESTION', 'PING_PONG_ANSWER');

-- ✅ 테이블 ------------------------------------------------------------

-- 핑퐁(질문) 테이블
CREATE TABLE ping_pong (
    id                 BIGSERIAL PRIMARY KEY,
    user_keyword_id    BIGINT        NOT NULL,
    questioner_id      BIGINT        NOT NULL,
    question           VARCHAR(2200) NOT NULL,
    question_hidden_at TIMESTAMPTZ, -- 신고 누적으로 숨김 처리된 시각
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ping_pong_user_keyword FOREIGN KEY (user_keyword_id) REFERENCES user_keyword(id) ON DELETE CASCADE,
    CONSTRAINT fk_ping_pong_questioner FOREIGN KEY (questioner_id) REFERENCES "user"(id) ON DELETE CASCADE
);

-- 핑퐁 답변 테이블 (질문당 최대 1개, 답변자는 게시물(user_keyword) 작성자로 한정)
-- 답변 삭제 후 재답변 시 새 id가 부여되어 답변 단위로 신고가 구분된다.
CREATE TABLE ping_pong_answer (
    id           BIGSERIAL PRIMARY KEY,
    ping_pong_id BIGINT        NOT NULL,
    answer       VARCHAR(2200) NOT NULL,
    hidden_at    TIMESTAMPTZ, -- 신고 누적으로 숨김 처리된 시각
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ping_pong_answer_ping_pong FOREIGN KEY (ping_pong_id) REFERENCES ping_pong(id) ON DELETE CASCADE,
    CONSTRAINT uq_ping_pong_answer_ping_pong UNIQUE (ping_pong_id)
);

-- 콘텐츠 신고 테이블 (신고 시점의 콘텐츠를 스냅샷으로 보관, 원본 삭제 후에도 유지)
CREATE TABLE content_report (
    id               BIGSERIAL PRIMARY KEY,
    reporter_id      BIGINT              NOT NULL,
    reported_user_id BIGINT              NOT NULL,
    content_type     content_report_type NOT NULL,
    content_id       BIGINT              NOT NULL, -- 다형성 참조이므로 FK 없음 (질문: ping_pong.id, 답변: ping_pong_answer.id)
    content_snapshot TEXT                NOT NULL,
    reason_id        BIGINT              NOT NULL,
    custom_reason    TEXT,
    created_at       TIMESTAMPTZ         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMPTZ         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_content_report_reporter FOREIGN KEY (reporter_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_content_report_reported_user FOREIGN KEY (reported_user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_content_report_reason FOREIGN KEY (reason_id) REFERENCES report_reason(id) ON DELETE RESTRICT,
    -- 한 사람이 한 콘텐츠에 대해 한 번만 신고 가능 (선행 컬럼으로 콘텐츠별 신고 수 집계에도 사용)
    CONSTRAINT uq_content_report_content_reporter UNIQUE (content_type, content_id, reporter_id),
    CONSTRAINT chk_content_report_not_self CHECK (reporter_id <> reported_user_id)
);

-- ✅ 인덱스 ------------------------------------------------------------

-- 게시물별 핑퐁 목록 조회 (id 역순 = 최신순)
CREATE INDEX idx_ping_pong_user_keyword_id ON ping_pong (user_keyword_id, id);

-- questioner_id 인덱스 미생성 (저장 용량 절약)
-- 계정 Hard Delete 시 CASCADE가 ping_pong을 seq scan 하므로, 배치가 느려지면 아래 인덱스를 추가한다.
-- CREATE INDEX idx_ping_pong_questioner_id ON ping_pong (questioner_id);

-- content_report는 신고 발생 빈도가 낮아 테이블이 작으므로 reporter_id / reported_user_id 인덱스를 두지 않는다.
-- 계정 Hard Delete가 느려지면 해당 컬럼 인덱스를 추가한다.
