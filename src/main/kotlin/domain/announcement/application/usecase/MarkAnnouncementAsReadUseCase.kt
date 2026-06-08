package com.turnin.domain.announcement.application.usecase

import com.turnin.common.model.id.AnnouncementId
import com.turnin.common.model.id.UserId
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.domain.announcement.domain.repository.AnnouncementRepository

/** 공지 읽음 처리 */
class MarkAnnouncementAsReadUseCase(private val announcementRepository: AnnouncementRepository) {
    /**
     * 공지 읽음 처리
     *
     * 이미 읽은 공지라면 무시한다.
     *
     * @param announcementId 공지 ID
     * @param userId 읽은 사용자 ID
     */
    suspend operator fun invoke(
        announcementId: AnnouncementId,
        userId: UserId,
    ) {
        LOGGER.debug("markAnnouncementAsRead called, announcementId: ${announcementId.value}, userId: ${userId.value}")
        announcementRepository.markAsRead(announcementId, userId)
    }
}

private val LOGGER = AppLoggerFactory.createLogger<MarkAnnouncementAsReadUseCase>()
