package com.peekr.domain.notification.domain.repository

import com.peekr.common.model.id.UserId
import com.peekr.domain.notification.domain.model.FcmToken

/** UserFcmToken 리포지토리 */
interface FcmTokenRepository {
    /**
     * FCM 토큰을 등록한다.
     * 이미 존재하는 토큰이면 is_active = true 로 업데이트, 없으면 새로 insert 한다.
     *
     * @param userId 토큰을 등록할 사용자 ID
     * @param token FCM 토큰
     */
    suspend fun upsert(userId: UserId, token: String): FcmToken

    /**
     * 특정 FCM 토큰을 비활성화한다. (로그아웃 시 호출)
     *
     * @param userId 토큰 소유자 ID
     * @param token 비활성화할 FCM 토큰
     * @return 비활성화 성공 시 true, 토큰을 찾지 못한 경우 false
     */
    suspend fun deactivate(userId: UserId, token: String): Boolean

    /**
     * 해당 사용자의 모든 FCM 토큰을 비활성화한다. (모든 기기 로그아웃 시 호출)
     *
     * @param userId 토큰을 비활성화할 사용자 ID
     */
    suspend fun deactivateAll(userId: UserId)

    /**
     * 해당 사용자의 모든 FCM 토큰을 삭제한다. (회원 탈퇴 시 호출)
     *
     * @param userId 토큰을 삭제할 사용자 ID
     */
    suspend fun deleteAll(userId: UserId)

    /**
     * 해당 사용자의 활성화된 FCM 토큰 목록을 조회한다. (알림 전송 시 호출)
     *
     * @param userId 조회할 사용자 ID
     * @return 활성화된 FCM 토큰 목록
     */
    suspend fun findActiveTokens(userId: UserId): List<String>
}
