package com.peekr.domain.auth.domain.service

import com.peekr.common.jwt.exception.TokenException
import com.peekr.domain.auth.exception.AuthException

interface RefreshTokenService {
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
        userId: Long,
        token: String,
    ): Boolean

    /**
     * JWT 형식의 토큰에서 String 타입의 UserId를 추출한다.
     *
     * @param token JWT 형식의 토큰
     * @return [Long] UserID, UserID 형식이 아니거나 추출하지 못한다면 null
     * @throws TokenException.CannotDecodedException 토큰이 정상적으로 디코딩 할 수 없는 형식인 경우 예외 발생
     */
    suspend fun extractUserId(token: String): Long?
}
