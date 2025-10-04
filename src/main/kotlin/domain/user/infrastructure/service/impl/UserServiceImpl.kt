package com.peekr.domain.user.infrastructure.service.impl

import com.peekr.common.model.UserId
import com.peekr.domain.user.domain.model.User
import com.peekr.domain.user.domain.model.UserPatch
import com.peekr.domain.user.domain.model.UserProfile
import com.peekr.domain.user.domain.repository.UserRepository
import com.peekr.domain.user.domain.service.UserService

class UserServiceImpl(private val userRepository: UserRepository) : UserService {
    override suspend fun getUserById(id: UserId): User? =
        userRepository.findById(id)

    override suspend fun getUserProfileById(id: UserId): UserProfile? =
        userRepository.findUserProfileById(id)

    override suspend fun updateUser(
        userId: UserId,
        patch: UserPatch,
    ): Boolean = userRepository.update(userId, patch)
}
