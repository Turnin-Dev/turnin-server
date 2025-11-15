package com.peekr.domain.user.application.usecase

import com.peekr.common.model.Introduce
import com.peekr.common.model.UserId
import com.peekr.domain.user.domain.repository.UserRepository

class UpdateIntroduceUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(
        userId: UserId,
        patchIntroduce: String,
    ): Boolean {
        val introduce = Introduce(patchIntroduce)
        return userRepository.updateIntroduce(userId, introduce)
    }
}
