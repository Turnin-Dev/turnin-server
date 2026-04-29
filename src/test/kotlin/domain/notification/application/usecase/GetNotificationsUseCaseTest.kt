package com.turnin.domain.notification.application.usecase

import com.turnin.common.model.id.UserId
import com.turnin.domain.notification.domain.repository.NotificationRepository
import com.turnin.domain.notification.notificationFixture
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
                notificationFixture(id = it.toLong(), userId = userId)
            }.sortedByDescending { it.id.value }

        coEvery {
            // 유스케이스가 pageSize + 1 로 요청
            notificationRepository.findByUserId(userId, cursor = null, size = pageSize + 1)
        } returns notifications

        // when
        val result = usecase(userId, cursor = null, pageSize = pageSize)

        // then
        assertEquals(pageSize, result.items.size)
        assertEquals(notifications[pageSize - 1].id.value, result.nextCursor)
    }

    @Test
    fun `다음 페이지가 없으면 nextCursor 는 null 이다`() = runTest {
        // given
        val userId = UserId(1L)
        val pageSize = 3
        val notifications = (1..2)
            .map {
                notificationFixture(id = it.toLong(), userId = userId)
            }.sortedByDescending { it.id.value }

        coEvery {
            // 유스케이스가 pageSize + 1 로 요청했지만 2개만 반환 → 다음 페이지 없음
            notificationRepository.findByUserId(userId, cursor = null, size = pageSize + 1)
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
        val pageSize = 3

        coEvery {
            notificationRepository.findByUserId(userId, cursor = null, size = pageSize + 1)
        } returns emptyList()

        // when
        val result = usecase(userId, cursor = null, pageSize = pageSize)

        // then
        assertTrue(result.items.isEmpty())
        assertNull(result.nextCursor)
    }
}
