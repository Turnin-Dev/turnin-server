package com.peekr.domain.user.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.user.domain.provider.AuthProvider
import com.peekr.domain.user.domain.provider.NotificationProvider
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class LogoutUseCaseTest {
    private val authProvider: AuthProvider = mockk()
    private val notificationProvider: NotificationProvider = mockk()
    private val usecase = LogoutUseCase(authProvider, notificationProvider)

    @Test
    fun `로그아웃 성공 테스트`() = runTest {
        // given
        coEvery { authProvider.deleteRefreshToken(TestUserId) } just Runs
        coEvery { notificationProvider.deactivate(TestUserId, any()) } just Runs

        // when
        val exception = runCatching {
            usecase(TestUserId.value, "fcm-token")
        }.exceptionOrNull()

        // then
        assertNull(exception)
    }

    @Test
    fun `토큰 비활성화 작업이 실패해도 로그아웃은 성공적으로 완료된다`() = runTest {
        // given
        coEvery { authProvider.deleteRefreshToken(TestUserId) } just Runs
        coEvery { notificationProvider.deactivate(TestUserId, any()) } throws RuntimeException("Error!")

        // when
        val exception = runCatching {
            usecase(TestUserId.value, "fcm-token")
        }.exceptionOrNull()

        // then
        assertNull(exception)
    }

    @Test
    fun `토큰이 빈 문자열이면 알림 해제를 수행하지 않는다`() = runTest {
        // given
        coEvery { authProvider.deleteRefreshToken(TestUserId) } just Runs

        // when
        usecase(TestUserId.value, "")

        // then
        coVerify(exactly = 0) { notificationProvider.deactivate(TestUserId, any()) }
    }

    @Test
    fun `토큰 삭제가 실패하면 예외가 전파된다`() = runTest {
        // given
        coEvery { authProvider.deleteRefreshToken(TestUserId) } throws RuntimeException("DB Error")

        // when
        val exception = runCatching {
            usecase(TestUserId.value, "fcm-token")
        }.exceptionOrNull()

        // then
        assertNotNull(exception)
        coVerify(exactly = 0) { notificationProvider.deactivate(TestUserId, any()) }
    }

    companion object {
        private val TestUserId = UserId(1L)
    }
}
