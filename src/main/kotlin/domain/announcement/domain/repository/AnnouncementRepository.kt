package com.turnin.domain.announcement.domain.repository

import com.turnin.common.model.AnnouncementAudience
import com.turnin.common.model.AnnouncementStatus
import com.turnin.common.model.id.AnnouncementId
import com.turnin.common.model.id.UserId
import com.turnin.domain.announcement.domain.model.Announcement
import com.turnin.domain.announcement.domain.model.AnnouncementDetail

/** 공지 리포지토리 */
interface AnnouncementRepository {
    /**
     * 활성 공지 목록 조회
     *
     * [userId]에게 노출 가능한 활성 공지 전체를 읽음 여부와 함께 반환한다.
     *
     * 만료된 공지([expires_at] 기준)는 제외된다.
     *
     * @param userId 조회 요청 사용자 ID
     * @param userRole 사용자 역할 (수신 대상 필터링)
     */
    suspend fun getAnnouncements(
        userId: UserId,
        userRole: AnnouncementAudience,
    ): List<Announcement>

    /**
     * 공지 생성
     *
     * @param detail 공지 상세정보
     */
    suspend fun createAnnouncement(detail: AnnouncementDetail)

    /**
     * 공지 읽음 처리
     *
     * 이미 읽은 공지라면 무시한다.
     *
     * @param announcementId 공지 ID
     * @param userId 읽은 사용자 ID
     */
    suspend fun markAsRead(
        announcementId: AnnouncementId,
        userId: UserId,
    )

    /**
     * 공지 상태 변경
     *
     * @param announcementId 공지 ID
     * @param status 변경할 상태
     */
    suspend fun updateStatus(
        announcementId: AnnouncementId,
        status: AnnouncementStatus,
    ): Boolean

    /**
     * 공지 삭제
     *
     * @param announcementId 삭제할 공지 ID
     */
    suspend fun deleteAnnouncement(announcementId: AnnouncementId): Boolean
}
