package com.peekr.domain.user.application.usecase

import com.peekr.domain.core.model.UserId
import com.peekr.domain.user.application.dto.UserDto
import com.peekr.domain.user.application.dto.toDto
import com.peekr.domain.user.domain.service.UserService

class UserUseCaseImpl(private val userService: UserService) : UserUseCase {
    override suspend fun getUserById(id: UserId): UserDto? {
        val user = userService.getUserById(id)
        return user?.toDto()
    }
}
