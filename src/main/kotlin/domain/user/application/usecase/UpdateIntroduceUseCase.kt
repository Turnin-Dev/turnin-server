package com.peekr.domain.user.application.usecase

import com.peekr.common.model.UserId
import com.peekr.domain.user.domain.repository.UserRepository

class UpdateIntroduceUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(
        userId: UserId,
        patchIntroduce: String,
    ): Boolean =
        userRepository.updateIntroduce(userId, patchIntroduce)
}
