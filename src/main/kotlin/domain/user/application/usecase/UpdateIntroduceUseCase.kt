package com.turnin.domain.user.application.usecase

import com.turnin.common.model.Introduce
import com.turnin.common.model.id.UserId
import com.turnin.domain.user.domain.repository.UserRepository

class UpdateIntroduceUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(
        userId: UserId,
        introduce: String,
    ): Boolean {
        val introduceVO = Introduce(introduce)
        return userRepository.updateIntroduce(userId, introduceVO)
    }
}
