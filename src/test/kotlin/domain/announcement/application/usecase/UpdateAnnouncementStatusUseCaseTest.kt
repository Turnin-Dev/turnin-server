package com.turnin.domain.announcement.application.usecase

import com.turnin.common.model.AnnouncementStatus
import com.turnin.common.model.id.AnnouncementId
import com.turnin.domain.announcement.domain.repository.AnnouncementRepository
import com.turnin.domain.announcement.exception.AnnouncementException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class UpdateAnnouncementStatusUseCaseTest {
    private val announcementRepository = mockk<AnnouncementRepository>()
    private lateinit var usecase: UpdateAnnouncementStatusUseCase

    @Before
    fun setUp() {
        usecase = UpdateAnnouncementStatusUseCase(announcementRepository)
    }

    @Test
    fun `공지 상태를 변경한다`() = runTest {
        // given
        coEvery { announcementRepository.updateStatus(TestAnnouncementId, AnnouncementStatus.ACTIVE) } returns true

        // when
        usecase(TestAnnouncementId, AnnouncementStatus.ACTIVE)

        // then
        coVerify(exactly = 1) { announcementRepository.updateStatus(TestAnnouncementId, AnnouncementStatus.ACTIVE) }
    }

    @Test
    fun `존재하지 않는 공지 상태 변경 시 예외가 발생한다`() = runTest {
        // given
        coEvery { announcementRepository.updateStatus(TestAnnouncementId, AnnouncementStatus.ACTIVE) } returns false

        // when, then
        assertThrows<AnnouncementException.NotFound> {
            usecase(TestAnnouncementId, AnnouncementStatus.ACTIVE)
        }
    }

    companion object {
        private val TestAnnouncementId = AnnouncementId(1L)
    }
}
