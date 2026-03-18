package com.peekr.domain.notification.application.usecase

import com.peekr.common.model.id.NotificationId
import com.peekr.common.model.id.UserId
import com.peekr.domain.notification.domain.repository.NotificationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkAsReadUseCaseTest {
    private val notificationRepository: NotificationRepository = mockk()
    private val useCase = MarkAsReadUseCase(notificationRepository)

    @Test
    fun `알림 읽음 처리 성공`() = runTest {
        // given
        val notificationId = NotificationId(1L)
        val userId = UserId(1L)

        coEvery { notificationRepository.markAsRead(notificationId, userId) } returns true

        // when
        val result = useCase(notificationId, userId)

        // then
        assertTrue(result)
    }

    @Test
    fun `존재하지 않는 알림 읽음 처리 시 false 반환`() = runTest {
        // given
        val notificationId = NotificationId(999L)
        val userId = UserId(1L)

        coEvery { notificationRepository.markAsRead(notificationId, userId) } returns false

        // when
        val result = useCase(notificationId, userId)

        // then
        assertFalse(result)
    }
}
