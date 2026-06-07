package com.turnin.common.jwt.domain.model

/** JWT 토큰에 넣을 클레임 이름 (키 값) */
enum class JWTClaimName(val key: String) {
    DISPLAY_ID("display_id"),
    ROLE("role"),
}
