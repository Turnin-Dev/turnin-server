package com.peekr.domain.user.infrastructure.service.impl

import com.peekr.common.model.UserId
import com.peekr.domain.user.domain.model.User
import com.peekr.domain.user.domain.repository.UserRepository
import com.peekr.domain.user.domain.service.UserService

class UserServiceImpl(private val userRepository: UserRepository) : UserService {
    override suspend fun getUserById(id: UserId): User? =
        userRepository.getUserById(id)
}
