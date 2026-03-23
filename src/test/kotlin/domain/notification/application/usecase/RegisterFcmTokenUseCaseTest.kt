package com.peekr.domain.notification.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.notification.application.dto.toDto
import com.peekr.domain.notification.domain.repository.FcmTokenRepository
import com.peekr.domain.notification.fcmTokenFixture
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RegisterFcmTokenUseCaseTest {
    private val fcmTokenRepository: FcmTokenRepository = mockk()
    private val usecase = RegisterFcmTokenUseCase(fcmTokenRepository)

    @Test
    fun `FCM 토큰 등록 성공`() = runTest {
        // given
        val userId = UserId(1L)
        val token = "test_fcm_token"
        val expectedFcmToken = fcmTokenFixture(userId = userId, token = token)

        coEvery { fcmTokenRepository.upsert(userId, token) } returns expectedFcmToken

        // when
        val result = usecase(userId, token)

        // then
        assertEquals(expectedFcmToken.toDto(), result)
        coVerify(exactly = 1) { fcmTokenRepository.upsert(userId, token) }
    }
}
