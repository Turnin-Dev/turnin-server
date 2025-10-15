package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.UserId
import com.peekr.common.model.UserKeywordId
import com.peekr.domain.userKeyword.application.dto.UserKeywordPatchDto
import com.peekr.domain.userKeyword.application.dto.toDomain
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository

/**
 * 사용자별 키워드를 업데이트한다.
 */
class UpdateUserKeywordUseCase(private val userKeywordRepository: UserKeywordRepository) {
    /**
     * @param ownerId 사용자 ID
     * @param userKeywordId [UserKeywordId] 사용자별 키워드 ID DTO
     * @param patch [UserKeywordPatchDto]
     *
     * @return 업데이트 성공 시 `true`를 반환하고 실패 시 `false`를 반환한다.
     */
    suspend operator fun invoke(
        ownerId: UserId,
        userKeywordId: UserKeywordId,
        patch: UserKeywordPatchDto,
    ): Boolean = userKeywordRepository.update(
        ownerId,
        userKeywordId,
        patch.toDomain(),
    )
}
