package com.peekr.domain.user.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.user.domain.provider.AuthProvider
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertDoesNotThrow

class LogoutUseCaseTest {
    private val authProvider: AuthProvider = mockk()
    private val usecase = LogoutUseCase(authProvider)

    @Test
    fun `로그아웃 성공 테스트`() = runTest {
        // given
        coEvery { authProvider.deleteRefreshToken(TestUserId) } just Runs

        // when, then
        assertDoesNotThrow {
            usecase(TestUserId.value)
        }
    }

    companion object {
        private val TestUserId = UserId(1L)
    }
}
