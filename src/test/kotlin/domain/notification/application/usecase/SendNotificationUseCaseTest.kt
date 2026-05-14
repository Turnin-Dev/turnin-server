package com.turnin.domain.notification.application.usecase

import com.turnin.common.firebase.FcmService
import com.turnin.common.model.NotificationType
import com.turnin.common.model.id.UserId
import com.turnin.domain.notification.application.dto.toDto
import com.turnin.domain.notification.domain.model.NotificationCommand
import com.turnin.domain.notification.domain.repository.FcmTokenRepository
import com.turnin.domain.notification.domain.repository.NotificationRepository
import com.turnin.domain.notification.exception.NotificationException
import com.turnin.domain.notification.notificationFixture
import com.turnin.util.db.TestDatabaseFactory
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class SendNotificationUseCaseTest {
    private val fcmTokenRepository: FcmTokenRepository = mockk()
    private val notificationRepository: NotificationRepository = mockk()
    private val fcmService: FcmService = mockk()
    private val usecase = SendNotificationUseCase(fcmTokenRepository, notificationRepository, fcmService)

    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @After
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `활성 토큰이 있으면 알림을 저장하고 FCM을 전송한다`() = runTest {
        // given
        val userId = UserId(1L)
        val command = NotificationCommand.personal(
            userId = userId,
            notiType = NotificationType.FRIEND_REQUEST,
            title = "친구 요청",
            message = "테스트 유저님이 친구 요청을 보냈어요.",
            imageUrl = "https://example.com/profile.jpg",
            refId = 2L,
            refType = "USER",
        )
        val expectedNotification = notificationFixture(userId = userId, command = command)

        coEvery { fcmTokenRepository.findActiveTokens(userId) } returns listOf("token1", "token2")
        coEvery { fcmService.sendToUsers(any(), any()) } just Runs
        coEvery { notificationRepository.save(command) } returns expectedNotification

        // when
        val result = usecase(command)

        // then
        assertEquals(expectedNotification.toDto(), result)
        coVerify(exactly = 1) { fcmService.sendToUsers(any(), any()) }
        coVerify(exactly = 1) { notificationRepository.save(command) }
    }

    @Test
    fun `활성 토큰이 없으면 FCM 전송 없이 알림만 저장한다`() = runTest {
        // given
        val userId = UserId(1L)
        val command = NotificationCommand.personal(
            userId = userId,
            notiType = NotificationType.FRIEND_REQUEST,
            title = "친구 요청",
            message = "테스트 유저님이 친구 요청을 보냈어요.",
        )
        val expectedNotification = notificationFixture(userId = userId, command = command)

        coEvery { fcmTokenRepository.findActiveTokens(userId) } returns emptyList()
        coEvery { notificationRepository.save(command) } returns expectedNotification

        // when
        val result = usecase(command)

        // then
        assertEquals(expectedNotification.toDto(), result)
        coVerify(exactly = 0) { fcmService.sendToUsers(any(), any()) } // FCM 전송 안 함
        coVerify(exactly = 1) { notificationRepository.save(command) }
    }

    @Test
    fun `userId가 없으면 예외가 발생한다`() = runTest {
        // given
        val command = NotificationCommand.broadcast(
            notiType = NotificationType.NOTICE,
            title = "공지",
            message = "공지 메시지",
        )

        // when, then
        assertThrows<NotificationException.MissingUserIdInPersonalNotification> {
            usecase(command)
        }
    }
}
