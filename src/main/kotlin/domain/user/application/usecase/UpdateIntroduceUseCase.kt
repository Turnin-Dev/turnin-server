package com.peekr.domain.user.application.usecase

import com.peekr.common.model.Introduce
import com.peekr.common.model.UserId
import com.peekr.domain.user.domain.repository.UserRepository

class UpdateIntroduceUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(
        userId: UserId,
        introduce: String,
    ): Boolean {
        val introduceVO = Introduce(introduce)
        return userRepository.updateIntroduce(userId, introduceVO)
    }
}
