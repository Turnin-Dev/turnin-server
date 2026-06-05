package com.turnin.domain.announcement.application.usecase

import com.turnin.common.model.AnnouncementStatus
import com.turnin.common.model.id.AnnouncementId
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.domain.announcement.domain.repository.AnnouncementRepository
import com.turnin.domain.announcement.exception.AnnouncementException

/** 공지 상태 변경 */
class UpdateAnnouncementStatusUseCase(private val announcementRepository: AnnouncementRepository) {
    /**
     * 공지 상태 변경
     *
     * @param announcementId 공지 ID
     * @param status 변경할 상태
     *
     * @throws AnnouncementException.NotFound 공지가 존재하지 않을 경우
     */
    suspend operator fun invoke(
        announcementId: AnnouncementId,
        status: AnnouncementStatus,
    ) {
        LOGGER.debug("updateAnnouncementStatus called, announcementId: ${announcementId.value}, status: $status")
        val updated = announcementRepository.updateStatus(announcementId, status)
        if (!updated) throw AnnouncementException.NotFound(announcementId)
    }
}

private val LOGGER = AppLoggerFactory.createLogger("UpdateAnnouncementStatusUseCase")
