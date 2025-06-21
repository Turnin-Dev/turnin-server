package com.peekr.domain.auth.application.usecase

import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.common.jwt.domain.model.entity.JWTToken
import com.peekr.domain.auth.application.dto.LoginDto
import com.peekr.domain.auth.application.dto.RegisterDto

interface AuthUseCase {
    /**
     * 소셜로그인
     *
     * @param loginDto [LoginDto]
     *
     * @return [JWTToken] 정상적으로 로그인이 진행된 경우
     * (로그인 실패 (사용자를 가져올 수 없는 경우) **`null`** 반환)
     */
    suspend fun login(loginDto: LoginDto): JWTTokenDto?

    /**
     * 회원가입
     *
     * @param registerDto [RegisterDto]
     *
     * @return [JWTToken] 정상적으로 회원가입이 진행된 경우
     */
    suspend fun register(registerDto: RegisterDto): JWTTokenDto
}
