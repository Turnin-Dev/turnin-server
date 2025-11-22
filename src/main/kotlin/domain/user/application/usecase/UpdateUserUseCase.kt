package com.peekr.domain.user.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.user.application.dto.UserPatchDto
import com.peekr.domain.user.application.dto.toDomain
import com.peekr.domain.user.domain.repository.UserRepository

/**
 * 사용자 정보를 수정한다.
 */
class UpdateUserUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(
        userId: UserId,
        patch: UserPatchDto,
    ): Boolean = userRepository.update(userId, patch.toDomain())
}
