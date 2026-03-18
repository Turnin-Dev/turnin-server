package com.peekr.domain.notification.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.notification.domain.repository.NotificationRepository
import com.peekr.domain.notification.notificationFixture
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetNotificationsUseCaseTest {
    private val notificationRepository: NotificationRepository = mockk()
    private val usecase = GetNotificationsUseCase(notificationRepository)

    @Test
    fun `다음 페이지가 있으면 nextCursor 를 반환한다`() = runTest {
        // given
        val userId = UserId(1L)
        val pageSize = 3
        val notifications = (1..4)
            .map {
                // size + 1 = 4개
                notificationFixture(id = it.toLong(), userId = userId)
            }.sortedByDescending { it.id.value }

        coEvery {
            notificationRepository.findByUserId(userId, cursor = null, size = pageSize)
        } returns notifications

        // when
        val result = usecase(userId, cursor = null, pageSize = pageSize)

        // then
        assertEquals(pageSize, result.items.size)
        // 3번째 id가 다음 커서
        assertEquals(notifications[pageSize - 1].id.value, result.nextCursor)
    }

    @Test
    fun `다음 페이지가 없으면 nextCursor 는 null 이다`() = runTest {
        // given
        val userId = UserId(1L)
        val pageSize = 3
        val notifications = (1..2)
            .map {
                // size + 1 보다 적음
                notificationFixture(id = it.toLong(), userId = userId)
            }.sortedByDescending { it.id.value }

        coEvery {
            notificationRepository.findByUserId(userId, cursor = null, size = pageSize)
        } returns notifications

        // when
        val result = usecase(userId, cursor = null, pageSize = pageSize)

        // then
        assertEquals(2, result.items.size)
        assertNull(result.nextCursor)
    }

    @Test
    fun `알림이 없으면 빈 리스트와 null 커서를 반환한다`() = runTest {
        // given
        val userId = UserId(1L)

        coEvery {
            notificationRepository.findByUserId(userId, cursor = null, size = 3)
        } returns emptyList()

        // when
        val result = usecase(userId, cursor = null, pageSize = 3)

        // then
        assertTrue(result.items.isEmpty())
        assertNull(result.nextCursor)
    }
}
