package com.peekr.domain.auth.application.dto

import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.common.model.UserId

/**
 * 회원가입 결과 DTO
 *
 * @property userId 사용자 ID
 * @property jwtTokenDto JWT 토큰 DTO
 */
data class RegisterResultDto(
    val userId: UserId,
    val jwtTokenDto: JWTTokenDto,
)
