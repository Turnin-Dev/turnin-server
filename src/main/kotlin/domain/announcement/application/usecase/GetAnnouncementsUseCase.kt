package com.turnin.domain.announcement.application.usecase

import com.turnin.common.model.AnnouncementAudience
import com.turnin.common.model.Role
import com.turnin.common.model.id.UserId
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.domain.announcement.application.dto.AnnouncementDto
import com.turnin.domain.announcement.application.dto.toDto
import com.turnin.domain.announcement.domain.repository.AnnouncementRepository

/** 활성 공지 목록 조회 */
class GetAnnouncementsUseCase(private val announcementRepository: AnnouncementRepository) {
    /**
     * 활성 공지 목록 조회
     *
     * [userId]에게 노출 가능한 활성 공지 전체를 읽음 여부와 함께 반환한다.
     *
     * @param userId 조회 요청 사용자 ID
     * @param userRole 사용자 역할 (수신 대상 필터링)
     *
     * @return [List]<[AnnouncementDto]>
     */
    suspend operator fun invoke(
        userId: UserId,
        userRole: Role,
    ): List<AnnouncementDto> {
        LOGGER.debug("getAnnouncements called, userId: ${userId.value}")
        val audiences = AnnouncementAudience.from(userRole)
        return announcementRepository.getAnnouncements(userId, audiences).map { it.toDto() }
    }
}

private val LOGGER = AppLoggerFactory.createLogger<GetAnnouncementsUseCase>()
