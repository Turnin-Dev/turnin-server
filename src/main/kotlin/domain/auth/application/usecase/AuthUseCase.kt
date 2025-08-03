package com.peekr.domain.auth.application.usecase

import com.peekr.common.db.scheme.SocialLoginProvider
import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.common.jwt.exception.TokenException
import com.peekr.domain.auth.application.dto.FindUserResultDto
import com.peekr.domain.auth.application.dto.LoginDto
import com.peekr.domain.auth.application.dto.RegisterDto

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
     * JWT 형식의 토큰에서 UserId를 추출한다.
     *
     * @param token JWT 형식의 토큰
     * @return [Long] UserID, UserID 형식이 아니거나 추출하지 못한다면 null
     * @throws TokenException.CannotDecodedException 토큰이 정상적으로 디코딩 할 수 없는 형식인 경우 예외 발생
     */
    suspend fun extractUserId(token: String): String?

    /**
     * 로그인을 수행하기 전에 이미 가입되어 있는 사용자인지 찾는다.
     *
     * @return [FindUserResultDto] 이미 가입된 사용자면 `true`, 신규 사용자면 'false'
     */
    suspend fun findUser(provider: SocialLoginProvider, providerId: String): FindUserResultDto
}
