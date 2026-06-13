package com.turnin.domain.announcement.application.usecase

import com.turnin.common.model.id.AnnouncementId
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.domain.announcement.domain.repository.AnnouncementRepository
import com.turnin.domain.announcement.exception.AnnouncementException

/** 공지 삭제 */
class DeleteAnnouncementUseCase(private val announcementRepository: AnnouncementRepository) {
    /**
     * 공지 삭제
     *
     * @param announcementId 삭제할 공지 ID
     *
     * @throws AnnouncementException.NotFound 공지가 존재하지 않을 경우
     */
    suspend operator fun invoke(announcementId: AnnouncementId) {
        LOGGER.debug("deleteAnnouncement called, announcementId: ${announcementId.value}")
        val deleted = announcementRepository.deleteAnnouncement(announcementId)
        if (!deleted) throw AnnouncementException.NotFound(announcementId)
    }
}

private val LOGGER = AppLoggerFactory.createLogger<DeleteAnnouncementUseCase>()
