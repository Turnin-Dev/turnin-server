package com.peekr.domain.auth.application.dto

import com.peekr.common.jwt.application.dto.JWTTokenDto
import com.peekr.common.model.id.UserId

/**
 * 로그인 결과 DTO
 *
 * @property userId 사용자 ID
 * @property jwtTokenDto JWT 토큰 DTO
 */
data class LoginResultDto(
    val userId: UserId,
    val jwtTokenDto: JWTTokenDto,
)
