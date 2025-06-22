package com.peekr.domain.user.infrastructure.serviceImpl

import com.peekr.domain.user.domain.model.User
import com.peekr.domain.user.domain.repository.UserRepository
import com.peekr.domain.user.domain.service.UserService
import com.peekr.domain.user.infrastructure.mapper.UserMapper

class UserServiceImpl(private val userRepository: UserRepository) : UserService {
    override suspend fun getUserById(id: Long): User? {
        val userEntity = userRepository.getUserById(id)
        return if (userEntity != null) {
            UserMapper.toDomain(userEntity.readValues)
        } else {
            null
        }
    }
}
