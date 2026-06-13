package com.turnin.domain.announcement.application.usecase

data class AnnouncementUseCases(
    /**
     * 공지 목록 조회
     *
     * @see GetAnnouncementsUseCase
     */
    val getAnnouncements: GetAnnouncementsUseCase,
    /**
     * 공지 알림 읽음 처리
     *
     * @see MarkAnnouncementAsReadUseCase
     */
    val markAsRead: MarkAnnouncementAsReadUseCase,
)
