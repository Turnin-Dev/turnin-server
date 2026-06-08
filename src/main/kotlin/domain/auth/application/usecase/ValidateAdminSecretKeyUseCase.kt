package com.turnin.domain.auth.application.usecase

import com.turnin.common.util.config.AppConfig
import com.turnin.domain.auth.exception.AuthException

/** 관리자 비밀키 검사 */
class ValidateAdminSecretKeyUseCase(private val appConfig: AppConfig) {
    /**
     * 관리자 비밀키 검사
     *
     * @param secretKey 비밀키
     */
    operator fun invoke(secretKey: String) {
        val expectedKey = appConfig.get("ktor.admin.secretKey")
            ?: error("Admin secret key must be provided")
        if (secretKey != expectedKey) throw AuthException.Unauthorized()
    }
}
