package com.peekr.domain.user.application.usecase

import com.peekr.common.model.UserId
import com.peekr.domain.user.application.dto.UserPatchDto
import com.peekr.domain.user.application.dto.toDomain
import com.peekr.domain.user.domain.service.UserService

/**
 * 사용자 정보를 수정한다.
 */
class UpdateUserUseCase(private val userService: UserService) {
    suspend operator fun invoke(
        userId: UserId,
        patch: UserPatchDto,
    ): Boolean = userService.updateUser(userId, patch.toDomain())
}
