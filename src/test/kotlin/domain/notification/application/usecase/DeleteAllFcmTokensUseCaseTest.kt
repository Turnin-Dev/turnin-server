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

class DeleteAllFcmTokensUseCaseTest {
    private val fcmTokenRepository: FcmTokenRepository = mockk()
    private val useCase = DeleteAllFcmTokensUseCase(fcmTokenRepository)

    @Test
    fun `모든 FCM 토큰 삭제 성공`() = runTest {
        // given
        val userId = UserId(1L)
        coEvery { fcmTokenRepository.deleteAll(userId) } just Runs

        // when
        useCase(userId)

        // then
        coVerify(exactly = 1) { fcmTokenRepository.deleteAll(userId) }
    }
}
