package com.peekr.domain.notification.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.common.util.pagination.cursor.CursorPage
import com.peekr.domain.notification.application.dto.NotificationDto
import com.peekr.domain.notification.application.dto.toDto
import com.peekr.domain.notification.domain.repository.NotificationRepository

/**
 * 알림 목록 조회
 *
 * @see invoke
 */
class GetNotificationsUseCase(private val notificationRepository: NotificationRepository) {
    /**
     * 알림 목록을 조회한다. (커서 기반 페이지네이션)
     * 개인 알림 + 브로드캐스트 알림을 최신순으로 함께 반환한다.
     *
     * @param userId 조회할 사용자 ID
     * @param cursor 커서 값 (알림 ID), null 이면 첫 페이지
     * @param pageSize 페이지 크기
     * @return [CursorPage]
     */
    suspend operator fun invoke(
        userId: UserId,
        cursor: Long?,
        pageSize: Int,
    ): CursorPage<NotificationDto, Long> {
        // 1. size + 1 개 조회
        val notificationsWithOneExtra = notificationRepository.findByUserId(
            userId = userId,
            cursor = cursor,
            size = pageSize,
        )

        // 2. 다음 페이지 존재 여부 확인 및 다음 커서 결정
        val hasNext = notificationsWithOneExtra.size > pageSize
        val notifications = if (hasNext) {
            notificationsWithOneExtra.take(pageSize)
        } else {
            notificationsWithOneExtra
        }
        val nextCursor = if (hasNext) notifications.last().id.value else null

        // 3. 결과 반환
        return CursorPage(
            items = notifications.map { it.toDto() },
            nextCursor = nextCursor,
        )
    }
}
