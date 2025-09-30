package com.peekr.domain.auth.domain.repository

import com.peekr.common.model.UserId
import com.peekr.domain.auth.exception.AuthException

interface RefreshTokenRepository {
    /**
     * 리프레쉬 토큰으로 사용자 ID를 찾는다.
     *
     * @param token 리프레쉬 토큰
     */
    suspend fun findUserIdByRefreshToken(token: String): UserId?

    /**
     * 리프레쉬 토큰을 저장한다.
     * ##### 반드시 유저가 존재하는지 확인 후 존재하는 사용자의 ID를 기준으로 저장해야 한다.
     *
     * @param userId 사용자 ID
     * @param token 리프레쉬 토큰
     *
     * @throws AuthException.CannotSaveRefreshTokenException 존재하지 않는 사용자의 ID로 토큰 저장 시 예외 발생
     */
    suspend fun save(
        userId: UserId,
        token: String,
    ): Boolean
}
