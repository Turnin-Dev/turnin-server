package com.turnin.domain.auth.application.usecase

import com.turnin.common.util.config.AppConfig
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.common.util.log.LogAction
import com.turnin.common.util.log.LogTag
import com.turnin.common.util.log.LogType
import com.turnin.domain.auth.exception.AuthException
import java.security.MessageDigest

/** 관리자 비밀키 검사 */
class ValidateAdminSecretKeyUseCase(private val appConfig: AppConfig) {
    val expectedKey = appConfig.get("ktor.admin.secretKey") ?: error("Admin secret key must be provided")

    /**
     * 관리자 비밀키 검사
     *
     * @param secretKey 비밀키
     */
    operator fun invoke(secretKey: String) {
        if (!MessageDigest.isEqual(secretKey.toByteArray(), expectedKey.toByteArray())) {
            LOGGER.warn(
                message = "Admin authentication failed: Invalid secret key",
                tags = mapOf(
                    LogTag.LOG_TYPE.key to LogType.NORMAL.value,
                    LogTag.ACTION.key to LogAction.ADMIN_AUTH_FAILURE.value,
                ),
            )
            throw AuthException.Unauthorized()
        }

        LOGGER.info(
            message = "Admin authentication successful",
            tags = mapOf(
                LogTag.LOG_TYPE.key to LogType.NORMAL.value,
                LogTag.ACTION.key to LogAction.ADMIN_AUTH_SUCCESS.value,
            ),
        )
    }
}

private val LOGGER = AppLoggerFactory.createLogger<ValidateAdminSecretKeyUseCase>()
