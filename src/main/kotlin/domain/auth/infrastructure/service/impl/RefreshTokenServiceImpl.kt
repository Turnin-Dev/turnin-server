package com.peekr.domain.auth.infrastructure.service.impl

import com.peekr.domain.auth.domain.repository.RefreshTokenRepository
import com.peekr.domain.auth.domain.service.RefreshTokenService
import com.peekr.domain.core.model.UserId

class RefreshTokenServiceImpl(private val refreshTokenRepository: RefreshTokenRepository) : RefreshTokenService {
    override suspend fun save(userId: UserId, token: String): Boolean =
        refreshTokenRepository.save(userId, token)
}
