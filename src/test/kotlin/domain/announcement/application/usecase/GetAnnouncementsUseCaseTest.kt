package com.turnin.domain.announcement.application.usecase

import com.turnin.common.model.AnnouncementAudience
import com.turnin.common.model.Role
import com.turnin.common.model.id.AnnouncementId
import com.turnin.common.model.id.UserId
import com.turnin.domain.announcement.application.dto.toDto
import com.turnin.domain.announcement.domain.model.Announcement
import com.turnin.domain.announcement.domain.repository.AnnouncementRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetAnnouncementsUseCaseTest {
    private val announcementRepository = mockk<AnnouncementRepository>()
    private lateinit var usecase: GetAnnouncementsUseCase

    @Before
    fun setUp() {
        usecase = GetAnnouncementsUseCase(announcementRepository)
    }

    @Test
    fun `일반 사용자는 ALL 대상 공지만 조회된다`() = runTest {
        // given
        val audiences = AnnouncementAudience.from(Role.USER)
        coEvery { announcementRepository.getAnnouncements(TestUserId, audiences) } returns listOf(TestAnnouncement)

        // when
        val result = usecase(TestUserId, Role.USER)

        // then
        assertEquals(listOf(TestAnnouncement.toDto()), result)
        coVerify(exactly = 1) { announcementRepository.getAnnouncements(TestUserId, audiences) }
    }

    @Test
    fun `어드민은 ALL과 ADMIN 대상 공지가 모두 조회된다`() = runTest {
        // given
        val audiences = AnnouncementAudience.from(Role.ADMIN)
        coEvery { announcementRepository.getAnnouncements(TestUserId, audiences) } returns
            listOf(TestAnnouncement, TestAdminAnnouncement)

        // when
        val result = usecase(TestUserId, Role.ADMIN)

        // then
        assertEquals(2, result.size)
        coVerify(exactly = 1) { announcementRepository.getAnnouncements(TestUserId, audiences) }
    }

    companion object {
        private val TestUserId = UserId(1L)
        private val TestAnnouncement = Announcement(
            id = AnnouncementId(1L),
            title = "전체 공지",
            content = "내용",
            targetAudience = AnnouncementAudience.ALL,
            expiresAt = null,
            createdAt = 1000L,
            isRead = false,
        )
        private val TestAdminAnnouncement = Announcement(
            id = AnnouncementId(2L),
            title = "관리자 공지",
            content = "내용",
            targetAudience = AnnouncementAudience.ADMIN,
            expiresAt = null,
            createdAt = 1000L,
            isRead = false,
        )
    }
}
