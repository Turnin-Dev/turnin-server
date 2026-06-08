package com.turnin.domain.auth.application.usecase

import com.turnin.common.util.config.AppConfig
import com.turnin.domain.auth.exception.AuthException
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class ValidateAdminSecretKeyUseCaseTest {
    private val appConfig = mockk<AppConfig>()
    private lateinit var usecase: ValidateAdminSecretKeyUseCase

    @Before
    fun setUp() {
        every { appConfig.get("ktor.admin.secretKey") } returns VALID_SECRET_KEY
        usecase = ValidateAdminSecretKeyUseCase(appConfig)
    }

    @Test
    fun `올바른 비밀키로 검증에 성공한다`() {
        // when, then
        usecase(VALID_SECRET_KEY)
    }

    @Test
    fun `잘못된 비밀키로 검증 시 예외가 발생한다`() {
        // when, then
        assertThrows<AuthException.Unauthorized> {
            usecase(INVALID_SECRET_KEY)
        }
    }

    @Test
    fun `비밀키가 설정되지 않은 경우 예외가 발생한다`() {
        // given
        val emptyConfig = mockk<AppConfig>()
        every { emptyConfig.get("ktor.admin.secretKey") } returns null

        // when, then
        assertThrows<IllegalStateException> {
            ValidateAdminSecretKeyUseCase(emptyConfig)
        }
    }

    companion object {
        private const val VALID_SECRET_KEY = "valid-secret-key"
        private const val INVALID_SECRET_KEY = "invalid-secret-key"
    }
}
