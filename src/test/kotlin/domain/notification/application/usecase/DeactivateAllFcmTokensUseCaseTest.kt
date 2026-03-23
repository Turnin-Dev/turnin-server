package com.peekr.domain.notification.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.notification.domain.repository.FcmTokenRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeactivateAllFcmTokensUseCaseTest {
    private val fcmTokenRepository: FcmTokenRepository = mockk()
    private val usecase = DeactivateAllFcmTokensUseCase(fcmTokenRepository)

    @Test
    fun `모든 FCM 토큰 비활성화 성공`() = runTest {
        // given
        val userId = UserId(1L)
        coEvery { fcmTokenRepository.deactivateAll(userId) } just Runs

        // when
        usecase(userId)

        // then
        coVerify(exactly = 1) { fcmTokenRepository.deactivateAll(userId) }
    }
}
