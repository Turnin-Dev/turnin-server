package com.peekr.domain.auth.application.usecase

import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.masking
import com.peekr.domain.auth.application.dto.FindUserResultDto
import com.peekr.domain.auth.application.mapper.AuthMapper.toDto
import com.peekr.domain.auth.domain.model.SocialLoginProviderForAuth
import com.peekr.domain.auth.domain.service.AuthService

/**
 * 로그인을 수행하기 전에 이미 가입되어 있는 사용자인지 찾는다.
 */
class FindUserUseCase(private val authService: AuthService) {
    /**
     * @param provider 소셜 로그인 제공자
     * @param providerId 소셜 로그인 제공자에서 제공한 ID
     *
     * @return [FindUserResultDto] 가입 여부(`exists`)
     */
    suspend operator fun invoke(
        provider: SocialLoginProviderForAuth,
        providerId: String,
    ): FindUserResultDto {
        LOGGER.debug("findUser called, provider: $provider, providerId: ${providerId.masking()}")
        val findUserResult = authService.findUser(provider, providerId)
        return findUserResult.toDto()
    }
}

private val LOGGER = AppLoggerFactory.createLogger("FindUserUseCase")
