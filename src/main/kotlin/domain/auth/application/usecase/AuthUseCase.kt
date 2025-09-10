package com.peekr.domain.auth.application.usecase

import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.domain.auth.application.dto.FindUserResultDto
import com.peekr.domain.auth.application.dto.LoginDto
import com.peekr.domain.auth.application.dto.RegisterDto
import com.peekr.domain.auth.domain.model.SocialLoginProviderForAuth

interface AuthUseCase {
    /**
     * 소셜로그인
     *
     * @param loginDto [LoginDto]
     *
     * @return [JWTTokenDto] 정상적으로 로그인이 진행된 경우
     * (로그인 실패 (사용자를 가져올 수 없는 경우) **`null`** 반환)
     */
    suspend fun login(loginDto: LoginDto): JWTTokenDto?

    /**
     * 회원가입
     *
     * @param registerDto [RegisterDto]
     *
     * @return [JWTTokenDto] 정상적으로 회원가입이 진행된 경우
     */
    suspend fun register(registerDto: RegisterDto): JWTTokenDto

    /**
     * 리프레쉬 토큰 갱신
     *
     * @param userId 사용자 ID
     * @param token 리프레쉬 토큰
     *
     * @return [JWTTokenDto] 정상적으로 리프레쉬 토큰이 갱신된 경우
     * (만약 리프레쉬 토큰 만료시 **`null`** 반환)
     */
    suspend fun refresh(userId: Long, token: String): JWTTokenDto?

    /**
     * 로그인을 수행하기 전에 이미 가입되어 있는 사용자인지 찾는다.
     *
     * @param provider 소셜 로그인 제공자
     * @param providerId 소셜 로그인 제공자에서 제공한 ID
     * @return [FindUserResultDto] 가입 여부(`exists`)
     */
    suspend fun findUser(provider: SocialLoginProviderForAuth, providerId: String): FindUserResultDto

    /**
     * 사용자 표시 ID의 존재 여부를 확인한다.
     *
     * @param displayId 사용자 표시 ID
     * @return 존재하면 `true`, 존재하지 않으면 `false`
     */
    suspend fun existsDisplayId(displayId: String): Boolean
}
