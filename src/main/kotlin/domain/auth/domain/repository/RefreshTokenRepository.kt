package com.peekr.domain.auth.domain.repository

interface RefreshTokenRepository {
    /**
     * 리프레쉬 토큰으로 사용자의 이름을 찾는다.
     *
     * @param token 리프레쉬 토큰
     */
    suspend fun findNameByRefreshToken(token: String): String?
}
