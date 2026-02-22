package com.peekr.domain.user.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.user.application.dto.UserPatchDto
import com.peekr.domain.user.application.dto.toDomain
import com.peekr.domain.user.domain.provider.FileProvider
import com.peekr.domain.user.domain.repository.UserRepository

/**
 * 사용자 정보를 수정한다.
 *
 * @see invoke
 */
class UpdateUserUseCase(
    private val userRepository: UserRepository,
    private val fileProvider: FileProvider,
) {
    /**
     * 사용자 정보를 수정한다.
     *
     * DB 업데이트 수행 이후에 기존 파일 삭제를 진행한다.
     * 이 순서는 바뀌면 안된다. (만약 바뀌게 되면 기존 파일 성공 후 DB 작업에 실패하는 경우 복구가 어렵다.)
     *
     * @param userId 수정할 사용자 ID
     * @param patch 사용자 정보 수정 패치
     */
    suspend operator fun invoke(
        userId: UserId,
        patch: UserPatchDto,
    ): Boolean {
        // 1. 사용자 정보 업데이트 수행
        val result = userRepository.update(userId, patch.toDomain())

        // 2. 프로필 사진 업데이트 유무 판별 후 파일 삭제 진행
        if (patch.oldProfileImageUrl != null &&
            patch.newProfileImageUrl != patch.oldProfileImageUrl
        ) {
            fileProvider.deleteFile(patch.oldProfileImageUrl)
        }

        return result
    }
}
