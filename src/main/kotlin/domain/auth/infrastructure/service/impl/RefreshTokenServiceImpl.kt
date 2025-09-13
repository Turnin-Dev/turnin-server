package com.peekr.domain.auth.infrastructure.service.impl

import com.peekr.domain.auth.domain.repository.RefreshTokenRepository
import com.peekr.domain.auth.domain.service.RefreshTokenService

class RefreshTokenServiceImpl(private val refreshTokenRepository: RefreshTokenRepository) : RefreshTokenService {
    override suspend fun save(userId: Long, token: String): Boolean =
        refreshTokenRepository.save(userId, token)
}
