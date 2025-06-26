package com.peekr.domain.auth.application.usecase

import com.peekr.common.jwt.application.dto.JWTTokenDto
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
     * @param token 리프레쉬 토큰
     *
     * @return [JWTTokenDto] 정상적으로 리프레쉬 토큰이 갱신된 경우
     * (만약 리프레쉬 토큰 만료시 **`null`** 반환)
     */
    suspend fun refresh(token: String): JWTTokenDto?
}
