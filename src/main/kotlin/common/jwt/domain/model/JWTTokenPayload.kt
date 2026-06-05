package com.turnin.common.jwt.domain.model

/**
 * JWT 토큰 페이로드
 *
 * @property userId 사용자 ID
 * @property claims 클레임 목록
 */
data class JWTTokenPayload(
    val userId: String,
    val claims: Map<JWTClaimName, String>,
)
