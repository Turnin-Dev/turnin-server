package com.peekr.domain.auth.application.usecase

import com.peekr.common.db.suspendTransaction
import com.peekr.common.jwt.application.dto.toDto
import com.peekr.common.jwt.domain.model.JWTClaimName
import com.peekr.common.jwt.domain.model.JWTTokenPayload
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.LogAction
import com.peekr.common.util.LogTag
import com.peekr.common.util.LogType
import com.peekr.common.util.masking
import com.peekr.domain.auth.application.dto.RegisterDto
import com.peekr.domain.auth.application.dto.RegisterResultDto
import com.peekr.domain.auth.application.mapper.AuthMapper.toDomain
import com.peekr.domain.auth.domain.model.Register
import com.peekr.domain.auth.domain.model.RegisterResult
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.domain.repository.RefreshTokenRepository
import com.peekr.domain.auth.exception.AuthException.DuplicateUserException

class RegisterUseCase(
    private val authRepository: AuthRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtTokenService: JWTTokenService,
) {
    /**
     * 회원가입
     *
     * @param registerDto [RegisterDto]
     *
     * @return [RegisterResultDto] 정상적으로 회원가입이 진행된 경우
     */
    suspend operator fun invoke(registerDto: RegisterDto): RegisterResultDto {
        LOGGER.info(
            message = "User registration attempt: displayId=${registerDto.displayId.masking()}",
            tags = mapOf(
                LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                LogTag.ACTION.key to LogAction.REGISTER_ATTEMPT.value,
            ),
        )

        val (savedAuthUser, registerResultDto) = suspendTransaction {
            val authUser = registerDto.toDomain()
            val registerResult = authRegister(authUser)
            val savedAuthUser = registerResult.authUser
            val jwtTokenDto = registerResult.jwtToken.toDto()

            // 리프레쉬 토큰 저장
            refreshTokenRepository.save(savedAuthUser.userId, jwtTokenDto.refreshToken)

            // 회원가입 성공 후 결과 반환
            savedAuthUser to RegisterResultDto(savedAuthUser.userId, jwtTokenDto)
        }

        LOGGER.info(
            message = "User registration successful: userId=${savedAuthUser.userId.value}",
            tags = mapOf(
                LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                LogTag.ACTION.key to LogAction.REGISTER_SUCCESS.value,
                LogTag.USER_ID.key to savedAuthUser.userId.value.toString(),
            ),
        )

        return registerResultDto
    }

    /**
     * 회원가입
     *
     * 단, 회원가입은 기존 회원이 존재하지 않는다는 가정하에 진행된다.
     *
     * @param register 회원가입 정보 [Register]
     *
     * @throws DuplicateUserException 이미 동일한 provider/providerId로 가입된 사용자가 존재하는 경우
     */
    private suspend fun authRegister(register: Register): RegisterResult {
        val savedAuthUser = authRepository.save(register)

        val payload = JWTTokenPayload(
            userId = savedAuthUser.userId.value.toString(),
            claimName = JWTClaimName.DISPLAY_ID,
            claim = savedAuthUser.displayId.value,
        )
        val jwtToken = jwtTokenService.generate(payload)

        return RegisterResult(jwtToken, savedAuthUser)
    }
}

private val LOGGER = AppLoggerFactory.createLogger("RegisterUseCase")
