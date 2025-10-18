package com.peekr.domain.auth.application.usecase

import com.peekr.common.model.DisplayId
import com.peekr.domain.auth.domain.service.AuthService
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class ExistsDisplayIdUseCaseTest {
    private val authService = mockk<AuthService>()
    private val usecase = ExistsDisplayIdUseCase(authService)

    @Test
    fun `사용자 표시 ID 존재 여부 확인 성공 테스트`() = runTest {
        // given
        coEvery { authService.existsDisplayId(any()) } returns true

        // then
        val result = usecase(TestDisplayId)

        // then
        assertTrue(result)
    }

    companion object {
        private val TestDisplayId = DisplayId("id")
    }
}
