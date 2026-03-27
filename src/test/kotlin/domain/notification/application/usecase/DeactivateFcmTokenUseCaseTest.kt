package com.peekr.domain.notification.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.notification.domain.repository.FcmTokenRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow

class DeactivateFcmTokenUseCaseTest {
    private val fcmTokenRepository: FcmTokenRepository = mockk()
    private val usecase = DeactivateFcmTokenUseCase(fcmTokenRepository)

    @Test
    fun `FCM 토큰 비활성화 성공`() = runTest {
        // given
        val userId = UserId(1L)
        val token = "test_fcm_token"

        coEvery { fcmTokenRepository.deactivate(userId, token) } just Runs

        // when & then
        assertDoesNotThrow { usecase(userId, token) }
    }

    @Test
    fun `존재하지 않는 토큰 비활성화 시 false 반환`() = runTest {
        // given
        val userId = UserId(1L)
        val token = "non_existent_token"

        coEvery { fcmTokenRepository.deactivate(userId, token) } just Runs

        // when & then
        assertDoesNotThrow { usecase(userId, token) }
    }
}
