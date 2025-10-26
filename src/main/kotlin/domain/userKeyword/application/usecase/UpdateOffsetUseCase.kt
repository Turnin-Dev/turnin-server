package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.UserId
import com.peekr.common.model.UserKeywordId
import com.peekr.domain.userKeyword.application.dto.UpdateOffsetDto
import com.peekr.domain.userKeyword.application.dto.toDomain
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository

/**
 * 사용자별 키워드 오프셋을 업데이트한다.
 */
class UpdateOffsetUseCase(private val userKeywordRepository: UserKeywordRepository) {
    /**
     * @param ownerId 사용자 ID
     * @param userKeywordId [UserKeywordId] 사용자별 키워드 ID
     * @param patch [UpdateOffsetDto] 오프셋 DTO
     *
     * @return 업데이트 성공 시 `true`를 반환하고 실패 시 `false`를 반환한다.
     */
    suspend operator fun invoke(
        ownerId: UserId,
        userKeywordId: UserKeywordId,
        patch: UpdateOffsetDto,
    ): UpdateOffsetDto? {
        val result = userKeywordRepository.updateOffset(
            ownerId,
            userKeywordId,
            patch.toDomain(),
        )

        return if (result) {
            return patch
        } else {
            null
        }
    }
}
