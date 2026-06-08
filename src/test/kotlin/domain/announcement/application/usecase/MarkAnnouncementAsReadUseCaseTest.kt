package com.turnin.domain.announcement.application.usecase

import com.turnin.common.model.id.AnnouncementId
import com.turnin.common.model.id.UserId
import com.turnin.domain.announcement.domain.repository.AnnouncementRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class MarkAnnouncementAsReadUseCaseTest {
    private val announcementRepository = mockk<AnnouncementRepository>()
    private lateinit var usecase: MarkAnnouncementAsReadUseCase

    @Before
    fun setUp() {
        usecase = MarkAnnouncementAsReadUseCase(announcementRepository)
    }

    @Test
    fun `공지를 읽음 처리한다`() = runTest {
        // given
        coEvery { announcementRepository.markAsRead(TestAnnouncementId, TestUserId) } just runs

        // when
        usecase(TestAnnouncementId, TestUserId)

        // then
        coVerify(exactly = 1) { announcementRepository.markAsRead(TestAnnouncementId, TestUserId) }
    }

    companion object {
        private val TestAnnouncementId = AnnouncementId(1L)
        private val TestUserId = UserId(1L)
    }
}
