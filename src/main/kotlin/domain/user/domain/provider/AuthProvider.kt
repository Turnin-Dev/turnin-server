package com.peekr.domain.user.domain.provider

import com.peekr.common.model.id.UserId

/**
 * 외부에서 제공되는 Auth BC API 인터페이스
 */
interface AuthProvider {
    /**
     * 리프레쉬 토큰을 삭제한다.
     *
     * @param userId 사용자 ID
     */
    suspend fun deleteRefreshToken(userId: UserId)
}
