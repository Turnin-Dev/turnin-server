package com.peekr.domain.notification.application.usecase

import com.peekr.common.firebase.FcmService
import com.peekr.common.model.NotificationType
import com.peekr.domain.notification.application.dto.toDto
import com.peekr.domain.notification.domain.model.NotificationCommand
import com.peekr.domain.notification.domain.repository.NotificationRepository
import com.peekr.domain.notification.notificationFixture
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SendBroadcastUseCaseTest {
    private val notificationRepository: NotificationRepository = mockk()
    private val fcmService: FcmService = mockk()
    private val usecase = SendBroadcastUseCase(notificationRepository, fcmService)

    @Test
    fun `브로드캐스트 알림 FCM 전송 후 저장한다`() = runTest {
        // given
        val command = NotificationCommand.broadcast(
            notiType = NotificationType.NOTICE,
            title = "공지사항",
            message = "서비스 점검 안내입니다.",
        )
        val expectedNotification = notificationFixture(userId = null, command = command)

        coEvery { fcmService.sendToTopic(any()) } returns true
        coEvery { notificationRepository.save(command) } returns expectedNotification

        // when
        val result = usecase(command)

        // then
        assertEquals(expectedNotification.toDto(), result)
        coVerify(exactly = 1) { fcmService.sendToTopic(any()) }
        coVerify(exactly = 1) { notificationRepository.save(command) }
    }
}
