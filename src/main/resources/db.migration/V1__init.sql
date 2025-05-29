-- ✅ ENUM ------------------------------------------------------------

-- 친구 상태
create type friend_status as ENUM ('pending', 'accepted', 'rejected');

-- ✅ 테이블 ------------------------------------------------------------

-- 사용자 테이블
CREATE TABLE "user" (
    id bigserial PRIMARY KEY,
    provider VARCHAR(50) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    name VARCHAR(50),
    nickname VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    profile_image_url VARCHAR(500),
    introduce TEXT,
    CONSTRAINT unique_provider_user UNIQUE (provider, provider_id)
);

-- 친구 테이블
CREATE TABLE friend (
    id bigserial PRIMARY KEY,
    requester_id bigint NOT NULL,
    receiver_id bigint NOT NULL,
    status friend_status NOT NULL DEFAULT 'pending',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    responded_at TIMESTAMP,
    CONSTRAINT fk_requester FOREIGN KEY (requester_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_receiver FOREIGN KEY (receiver_id) REFERENCES "user"(id) ON DELETE CASCADE
);

-- 알림 테이블
CREATE TABLE notification (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL,
    noti_type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_notify_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

-- 키워드 테이블
CREATE TABLE keyword (
    id bigserial PRIMARY KEY,
    keyword VARCHAR(100) NOT NULL UNIQUE,
    created_by bigint NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_keyword_creator FOREIGN KEY (created_by) REFERENCES "user"(id) ON DELETE CASCADE
);

-- 키워드 댓글 테이블
CREATE TABLE keyword_comment (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL,
    keyword_id bigint NOT NULL,
    comment TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_keyword FOREIGN KEY (keyword_id) REFERENCES keyword(id) ON DELETE CASCADE
);

-- 사용자별 키워드 테이블
CREATE TABLE user_keyword (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL,
    keyword_id bigint NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_userkeyword_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_userkeyword_keyword FOREIGN KEY (keyword_id) REFERENCES keyword(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_keyword UNIQUE (user_id, keyword_id)
);

-- 신고 사유 테이블 (룩업 테이블 방식)
CREATE TABLE report_reason (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL, -- 예: SPAM, OFFENSIVE, ETC
    description TEXT NOT NULL
);

-- 신고 테이블
CREATE TABLE report (
    id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    reported_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    reason_id BIGINT NOT NULL REFERENCES report_reason(id),
    custom_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 차단 사유 테이블 (룩업 테이블 방식)
CREATE TABLE block_reason (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL, -- 예: ABUSE, SPAM, ETC
    description TEXT NOT NULL
);

-- 차단 테이블
CREATE TABLE block (
    id BIGSERIAL PRIMARY KEY,
    blocker_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    blocked_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    reason_id BIGINT NOT NULL REFERENCES block_reason(id),
    custom_reason TEXT,
    is_blocked BOOLEAN DEFAULT TRUE, -- 차단 상태 여부
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 리프레쉬 토큰
CREATE TABLE refresh_tokens (
    user_id BIGINT PRIMARY KEY REFERENCES "user"(id) ON DELETE CASCADE,
    refresh_token TEXT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ✅ 인덱스 ------------------------------------------------------------

-- 복합 인덱스
CREATE INDEX idx_friend_requester_receiver ON friend (requester_id, receiver_id);
CREATE INDEX idx_friend_receiver_requester ON friend (receiver_id, requester_id);
CREATE INDEX idx_friend_status ON friend (status);

-- (선택적) 인덱스: 키워드별 댓글 조회 최적화
CREATE INDEX idx_keywordcomment_keyword_created ON keyword_comment (keyword_id, created_at);

-- 인덱스 추가 (조회 성능 최적화)
CREATE INDEX idx_userkeyword_keyword_user ON user_keyword (keyword_id, user_id);

-- 성능을 위한 인덱스 및 신고 중복 확인용
CREATE INDEX idx_report_pair ON report (reporter_id, reported_id);

-- 유저 간 차단 상태 확인, 중복 방지용 인덱스
CREATE INDEX idx_block_pair ON block (blocker_id, blocked_id);

-- 키워드 작성자 조회용 인덱스
CREATE INDEX idx_keyword_created_by ON keyword(created_by);
