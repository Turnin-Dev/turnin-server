package com.turnin.domain.announcement.application.usecase

import com.turnin.common.model.AnnouncementStatus
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.domain.announcement.domain.model.AnnouncementDetail
import com.turnin.domain.announcement.domain.repository.AnnouncementRepository

/** 공지 생성 */
class CreateAnnouncementUseCase(private val announcementRepository: AnnouncementRepository) {
    /**
     * 공지 생성
     *
     * 생성된 공지는 기본적으로 [AnnouncementStatus.INACTIVE] 상태로 생성된다.
     *
     * @param detail 공지 생성 정보
     */
    suspend operator fun invoke(detail: AnnouncementDetail) {
        LOGGER.debug("createAnnouncement called, title: ${detail.title}")
        announcementRepository.createAnnouncement(detail)
    }
}

private val LOGGER = AppLoggerFactory.createLogger<CreateAnnouncementUseCase>()
