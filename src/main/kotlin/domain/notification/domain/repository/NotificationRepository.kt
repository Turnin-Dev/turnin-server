package com.turnin.domain.notification.domain.repository

import com.turnin.common.model.id.NotificationId
import com.turnin.common.model.id.UserId
import com.turnin.domain.notification.domain.model.Notification
import com.turnin.domain.notification.domain.model.NotificationCommand

/** Notification 리포지토리 */
interface NotificationRepository {
    /**
     * 알림을 저장한다.
     * 브로드캐스트 알림의 경우 [NotificationCommand.userId] 는 null 이다.
     *
     * @param command 저장할 알림 도메인 모델
     */
    suspend fun save(command: NotificationCommand): Notification

    /**
     * 해당 사용자의 알림 목록을 최신순으로 조회한다. (커서 기반 페이지네이션)
     *
     * 개인 알림과 브로드캐스트 알림을 함께 반환한다.
     *
     * ##### 브로드캐스트 알림 읽음 처리
     * 브로드캐스트 알림([Notification.isBroadcast] = true)은 읽음 처리를 지원하지 않는다.
     * 클라이언트에서 [Notification.isBroadcast] 여부를 확인하여 읽음 처리 UI를 숨겨야 한다.
     *
     * @param userId 조회할 사용자 ID
     * @param cursor 커서 값 (알림 ID), null 이면 첫 페이지
     * @param size 페이지 크기
     * @return 알림 목록 (최신순)
     */
    suspend fun findByUserId(
        userId: UserId,
        cursor: Long?,
        size: Int,
    ): List<Notification>

    /**
     * 특정 알림을 읽음 처리한다.
     * 본인의 알림만 읽음 처리할 수 있도록 [userId] 로 소유권을 검증한다.
     *
     * ##### 브로드캐스트 알림
     * 브로드캐스트 알림은 [userId]가 null이므로 읽음 처리가 불가능하다. (의도된 설계)
     * 클라이언트에서 브로드캐스트 알림에 대한 읽음 처리 UI를 제공하지 않아야 한다.
     *
     * @param notificationId 읽음 처리할 알림 ID
     * @param userId 요청한 사용자 ID
     * @return 읽음 처리 성공 시 true, 알림을 찾지 못한 경우 false
     */
    suspend fun markAsRead(notificationId: NotificationId, userId: UserId): Boolean

    /**
     * 해당 사용자의 모든 알림을 삭제한다. (회원 탈퇴 시 호출)
     *
     * @param userId 알림을 삭제할 사용자 ID
     */
    suspend fun deleteAll(userId: UserId)
}
