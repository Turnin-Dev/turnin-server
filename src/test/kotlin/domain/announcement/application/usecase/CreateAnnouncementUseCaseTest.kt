package com.turnin.domain.announcement.application.usecase

import com.turnin.common.model.AnnouncementAudience
import com.turnin.domain.announcement.domain.model.AnnouncementDetail
import com.turnin.domain.announcement.domain.repository.AnnouncementRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CreateAnnouncementUseCaseTest {
    private val announcementRepository = mockk<AnnouncementRepository>()
    private lateinit var usecase: CreateAnnouncementUseCase

    @Before
    fun setUp() {
        usecase = CreateAnnouncementUseCase(announcementRepository)
    }

    @Test
    fun `공지를 생성한다`() = runTest {
        // given
        coEvery { announcementRepository.createAnnouncement(TestAnnouncementDetail) } just runs

        // when
        usecase(TestAnnouncementDetail)

        // then
        coVerify(exactly = 1) { announcementRepository.createAnnouncement(TestAnnouncementDetail) }
    }

    companion object {
        private val TestAnnouncementDetail = AnnouncementDetail(
            title = "테스트 공지",
            content = "테스트 내용",
            targetAudience = AnnouncementAudience.ALL,
            expiresAt = null,
        )
    }
}
