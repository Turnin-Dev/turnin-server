package com.peekr.domain.user.domain.provider

import com.peekr.common.model.id.UserId

/**
 * 외부에서 제공받은 알림 API
 */
interface NotificationProvider {
    /**
     * 특정 FCM 토큰을 비활성화한다. (로그아웃 시 호출)
     *
     * @param userId 토큰 소유자 ID
     * @param token 비활성화할 FCM 토큰
     */
    suspend fun deactivate(userId: UserId, token: String)
}
