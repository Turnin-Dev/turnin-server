package com.peekr.domain.user.application.usecase

import com.peekr.domain.core.model.UserId
import com.peekr.domain.user.application.dto.UserDto

interface UserUseCase {
    suspend fun getUserById(id: UserId): UserDto?
}
