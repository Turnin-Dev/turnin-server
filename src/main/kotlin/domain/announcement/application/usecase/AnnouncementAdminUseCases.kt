package com.turnin.domain.announcement.application.usecase

data class AnnouncementAdminUseCases(
    /**
     * 공지 생성
     *
     * @see CreateAnnouncementUseCase
     */
    val create: CreateAnnouncementUseCase,
    /**
     * 공지 수정
     *
     * @see UpdateAnnouncementStatusUseCase
     */
    val update: UpdateAnnouncementStatusUseCase,
    /**
     * 공지 삭제
     *
     * @see DeleteAnnouncementUseCase
     */
    val delete: DeleteAnnouncementUseCase,
)
