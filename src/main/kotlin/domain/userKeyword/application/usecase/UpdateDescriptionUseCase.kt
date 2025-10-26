package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.UserId
import com.peekr.common.model.UserKeywordId
import com.peekr.domain.userKeyword.application.dto.DescriptionDto
import com.peekr.domain.userKeyword.application.dto.toDomain
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository

/**
 * 사용자별 키워드 설명을 업데이트한다.
 */
class UpdateDescriptionUseCase(private val userKeywordRepository: UserKeywordRepository) {
    /**
     * @param ownerId 사용자 ID
     * @param userKeywordId [UserKeywordId] 사용자별 키워드 ID
     * @param patch [DescriptionDto] 키워드 설명 DTO
     *
     * @return 업데이트 성공 시 [DescriptionDto]를 반환하고 실패 시 `null`를 반환한다.
     */
    suspend operator fun invoke(
        ownerId: UserId,
        userKeywordId: UserKeywordId,
        patch: DescriptionDto,
    ): DescriptionDto? {
        val result = userKeywordRepository.updateDescription(
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
