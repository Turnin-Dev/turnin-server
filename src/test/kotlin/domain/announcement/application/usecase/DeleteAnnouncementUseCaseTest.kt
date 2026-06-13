package com.turnin.domain.announcement.application.usecase

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

class DeleteAnnouncementUseCaseTest {
    private val announcementRepository = mockk<AnnouncementRepository>()
    private lateinit var usecase: DeleteAnnouncementUseCase

    @Before
    fun setUp() {
        usecase = DeleteAnnouncementUseCase(announcementRepository)
    }

    @Test
    fun `공지를 삭제한다`() = runTest {
        // given
        coEvery { announcementRepository.deleteAnnouncement(TestAnnouncementId) } returns true

        // when
        usecase(TestAnnouncementId)

        // then
        coVerify(exactly = 1) { announcementRepository.deleteAnnouncement(TestAnnouncementId) }
    }

    @Test
    fun `존재하지 않는 공지 삭제 시 예외가 발생한다`() = runTest {
        // given
        coEvery { announcementRepository.deleteAnnouncement(TestAnnouncementId) } returns false

        // when, then
        assertThrows<AnnouncementException.NotFound> {
            usecase(TestAnnouncementId)
        }
    }

    companion object {
        private val TestAnnouncementId = AnnouncementId(1L)
    }
}
