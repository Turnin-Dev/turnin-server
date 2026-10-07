package com.turnin.domain.announcement.application.provider

import com.turnin.common.model.id.UserId
import com.turnin.domain.announcement.domain.repository.AnnouncementRepository

/**
 * 외부에 제공할 Announcement 삭제 제공 API
 */
class AnnouncementDeletionSupportApi(private val announcementRepository: AnnouncementRepository) {
    /**
     * 사용자의 공지 읽음 기록을 전부 삭제한다.
     *
     * @param userId 사용자 ID
     */
    suspend fun deleteReadsByUserId(userId: UserId) =
        announcementRepository.deleteReadsByUserId(userId)
}
