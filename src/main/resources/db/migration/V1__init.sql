-- ✅ 확장 활성화 ------------------------------------------------------------
-- pgvector
create EXTENSION IF NOT EXISTS vector;

-- ✅ ENUM ------------------------------------------------------------

-- 친구 상태
create type friend_status as ENUM ('PENDING', 'ACCEPTED', 'REJECTED');

-- 소셜로그인 제공업체
create type social_login_provider as ENUM ('GOOGLE', 'APPLE', 'KAKAO');

-- 역할
create type user_role as ENUM('USER', 'ADMIN');

-- 키워드 카테고리
CREATE TYPE keyword_category AS ENUM (
    'FOOD', 'FASHION', 'BEAUTY', 'TRAVEL', 'FITNESS', 'DAILY',
    'ROMANCE', 'CAREER', 'TECH', 'ART', 'ENTERTAINMENT',
    'SELF_DEVELOPMENT', 'EMOTION'
);

-- ✅ 테이블 ------------------------------------------------------------

-- 사용자 테이블
CREATE TABLE "user" (
    id BIGSERIAL PRIMARY KEY,
    role user_role NOT NULL DEFAULT 'USER',
    provider social_login_provider NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    display_id VARCHAR(30) UNIQUE NOT NULL,
    name VARCHAR(30) NOT NULL,
    profile_image_url VARCHAR(500),
    introduce TEXT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE, -- 탈퇴/정지 사용자 관리
    last_login_at TIMESTAMPTZ NOT NULL, -- 마지막 로그인 추적
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_provider_user UNIQUE (provider, provider_id),
    CONSTRAINT uq_display_id UNIQUE (display_id)
);

-- 친구 테이블
CREATE TABLE friend (
    id BIGSERIAL PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    status friend_status NOT NULL DEFAULT 'PENDING',
    responded_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_requester FOREIGN KEY (requester_id) REFERENCES "user"(id) ON DELETE RESTRICT,
    CONSTRAINT fk_receiver FOREIGN KEY (receiver_id) REFERENCES "user"(id) ON DELETE RESTRICT,
    CONSTRAINT uq_friend_requester_receiver UNIQUE (requester_id, receiver_id),
    CONSTRAINT chk_friend_not_self CHECK (requester_id != receiver_id)
);

-- FCM 토큰 테이블
CREATE TABLE user_fcm_token (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    token       TEXT NOT NULL,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fcm_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT uq_fcm_token UNIQUE (token)
);

-- 알림 테이블
CREATE TABLE notification (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT,
    noti_type    VARCHAR(50) NOT NULL,
    title        VARCHAR(200),
    message      TEXT NOT NULL,
    image_url 	 VARCHAR(500),
    is_read      BOOLEAN NOT NULL DEFAULT FALSE,
    is_broadcast BOOLEAN NOT NULL DEFAULT FALSE,
    ref_id       BIGINT,
    ref_type     VARCHAR(50),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

-- 키워드 테이블
CREATE TABLE keyword (
    id BIGSERIAL PRIMARY KEY,
    keyword VARCHAR(100) NOT null UNIQUE,
    embedding vector(768) NOT NULL,
    category keyword_category,
    category_similarity DOUBLE PRECISION,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_keyword_creator FOREIGN KEY (created_by) REFERENCES "user"(id) ON DELETE RESTRICT
);

-- 사용자별 키워드 테이블
CREATE TABLE user_keyword (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    keyword_id BIGINT NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_userkeyword_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE RESTRICT,
    CONSTRAINT fk_userkeyword_keyword FOREIGN KEY (keyword_id) REFERENCES keyword(id) ON DELETE CASCADE
);

-- 키워드 댓글 테이블
--CREATE TABLE keyword_comment (
--    id BIGSERIAL PRIMARY KEY,
--    user_id BIGINT NOT NULL,
--    user_keyword_id BIGINT NOT NULL,
--    comment TEXT NOT NULL,
--    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
--    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
--    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE RESTRICT,
--    CONSTRAINT fk_comment_keyword FOREIGN KEY (user_keyword_id) REFERENCES user_keyword(id) ON DELETE CASCADE
--);

-- 신고 사유 테이블 (룩업 테이블)
-- code 예: SPAM, OFFENSIVE, ETC
CREATE TABLE report_reason (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT NOT NULL
);

-- 신고 테이블
CREATE TABLE report (
    id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE RESTRICT,

    -- 각각에 대해 FK 설정 (신고 대상은 적어도 하나라도 데이터가 있어야 함)
    reported_id BIGINT REFERENCES "user"(id) ON DELETE RESTRICT,
    reported_user_keyword_id BIGINT REFERENCES user_keyword(id) ON DELETE RESTRICT,

    reason_id BIGINT NOT NULL REFERENCES report_reason(id) ON DELETE RESTRICT,
    custom_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 한 사람이 한 대상에 대해 한 번만 신고가능하도록 제약, 신고 대상은 하나라도 있어야 한다.
    CONSTRAINT chk_report_target_present
        CHECK (reported_id IS NOT NULL OR reported_user_keyword_id IS NOT NULL),
    CONSTRAINT uq_report_reporter_user UNIQUE (reporter_id, reported_id),
    CONSTRAINT uq_report_reporter_keyword UNIQUE (reporter_id, reported_user_keyword_id)
);

-- 차단 사유 테이블 (룩업 테이블)
-- code 예: ABUSE, SPAM, ETC
CREATE TABLE block_reason (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT NOT NULL
);

-- 차단 테이블
CREATE TABLE block (
    id BIGSERIAL PRIMARY KEY,
    blocker_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE RESTRICT,
    blocked_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE RESTRICT,
    reason_id BIGINT NOT NULL REFERENCES block_reason(id) ON DELETE RESTRICT,
    custom_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_block_pair UNIQUE (blocker_id, blocked_id)
);

-- 리프레시 토큰
CREATE TABLE refresh_tokens (
    user_id BIGINT PRIMARY KEY REFERENCES "user"(id) ON DELETE CASCADE,
    refresh_token TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_refresh_tokens_token UNIQUE (refresh_token)
);

-- ✅ 필수 인덱스 ------------------------------------------------------------

-- 키워드별 댓글 조회를 위한 인덱스 (댓글 타임라인)
-- CREATE INDEX idx_keyword_comment_keyword_created ON keyword_comment (user_keyword_id, created_at);

-- 친구 관계 조회를 위한 인덱스
CREATE INDEX idx_friend_receiver_status ON friend (receiver_id, status);

-- 알림 조회를 위한 인덱스 (읽지 않은 알림 위주)
CREATE INDEX idx_notification_user_unread ON notification (user_id, is_read, created_at);

-- 리프레쉬 토큰 인덱스 (제거 예정)
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

-- 사용자 키워드 인덱스, 키워드 네트워크 그래프 조회에 필요한 핵심 인덱스
CREATE UNIQUE INDEX IF NOT EXISTS uq_user_id_keyword_id ON user_keyword (user_id, keyword_id);
CREATE INDEX IF NOT EXISTS idx_user_keyword_combo_keyword ON user_keyword (keyword_id, user_id);

-- 키워드 카테고리 유사도 인덱스
CREATE INDEX idx_keyword_category_similarity
    ON keyword (category, category_similarity DESC);

-- FCM 알림 인덱스
CREATE INDEX idx_fcm_token_user_id ON user_fcm_token (user_id);
CREATE INDEX idx_fcm_token_active_only ON user_fcm_token (user_id) WHERE is_active = TRUE;
CREATE INDEX idx_notification_user_id ON notification (user_id);
CREATE INDEX idx_notification_unread ON notification (user_id, is_read) WHERE is_read = FALSE;
CREATE INDEX idx_notification_broadcast ON notification (is_broadcast);

-- ✅ 추가 인덱스 ------------------------------------------------------------

-- 친구 키워드 조회 (양방향)
CREATE INDEX idx_friend_accepted_requester
ON friend (requester_id, status)
WHERE status = 'ACCEPTED';

-- user_keyword 시간순 정렬 최적화
CREATE INDEX idx_user_keyword_created_desc
ON user_keyword (created_at DESC, user_id);

-- 차단 사용자 필터링
CREATE INDEX idx_block_lookup ON block (blocker_id, blocked_id);
CREATE INDEX idx_block_reverse_lookup ON block (blocked_id, blocker_id);

-- ✅ 초기 데이터 ------------------------------------------------------------

-- 신고 사유
INSERT INTO report_reason (code, description) VALUES
    ('SPAM', '스팸 및 사기'),
    ('HARASSMENT', '욕설 및 괴롭힘'),
    ('ILLEGAL', '불법 / 위험 행위'),
    ('PRIVACY', '개인정보 침해'),
    ('NUDITY', '음란성 / 선정성'),
    ('VIOLENCE', '폭력성 / 혐오 표현'),
    ('INAPPROPRIATE', '부적절한 내용'),
    ('NOT_INTERESTED', '관심 없는 내용 / 그냥 보기 싫음'),
    ('ETC', '기타');

-- 차단 사유
INSERT INTO block_reason (code, description) VALUES
    ('SPAM', '스팸 및 사기'),
    ('HARASSMENT', '욕설 및 괴롭힘'),
    ('ILLEGAL', '불법 / 위험 행위'),
    ('PRIVACY', '개인정보 침해'),
    ('NUDITY', '음란성 / 선정성'),
    ('VIOLENCE', '폭력성 / 혐오 표현'),
    ('INAPPROPRIATE', '부적절한 내용'),
    ('NOT_INTERESTED', '그냥 보기 싫음'),
    ('ETC', '기타');