package com.peekr.domain.auth.application.usecase

import com.peekr.common.model.DisplayId
import com.peekr.domain.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class ExistsDisplayIdUseCaseTest {
    private val authRepository = mockk<AuthRepository>()
    private val usecase = ExistsDisplayIdUseCase(authRepository)

    @Test
    fun `사용자 표시 ID 존재 여부 확인 성공 테스트`() = runTest {
        // given
        coEvery { authRepository.existsByDisplayId(TestDisplayId) } returns true

        // when
        val result = usecase(TestDisplayId)

        // then
        assertTrue(result)
    }

    @Test
    fun `사용자 표시 ID 존재 여부 확인 실패 테스트`() = runTest {
        // given
        coEvery { authRepository.existsByDisplayId(TestDisplayId) } returns false

        // when
        val result = usecase(TestDisplayId)

        // then
        assertFalse(result)
    }

    companion object {
        private val TestDisplayId = DisplayId("id")
    }
}
