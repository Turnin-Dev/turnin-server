package com.peekr.domain.user.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.user.domain.provider.AuthProvider
import com.peekr.domain.user.domain.provider.NotificationProvider
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertNull

class LogoutUseCaseTest {
    private val authProvider: AuthProvider = mockk()
    private val notificationProvider: NotificationProvider = mockk()
    private val usecase = LogoutUseCase(authProvider, notificationProvider)

    @Test
    fun `로그아웃 성공 테스트`() = runTest {
        // given
        coEvery { authProvider.deleteRefreshToken(TestUserId) } just Runs
        coEvery { notificationProvider.deactivate(TestUserId, any()) } returns true

        // when
        val exception = runCatching {
            usecase(TestUserId.value, "fcm-token")
        }.exceptionOrNull()

        // then
        assertNull(exception)
    }

    companion object {
        private val TestUserId = UserId(1L)
    }
}
