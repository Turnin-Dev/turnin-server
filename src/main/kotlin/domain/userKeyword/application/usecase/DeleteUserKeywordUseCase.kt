package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.UserId
import com.peekr.common.model.UserKeywordId
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository

/**
 * 사용자별 키워드 ID로 사용자별 키워드를 삭제한다.
 */
class DeleteUserKeywordUseCase(private val userKeywordRepository: UserKeywordRepository) {
    /**
     * @param ownerId 사용자 ID
     * @param userKeywordId 사용자별 키워드 ID DTO
     *
     * @return 삭제 성공시 `true`를 반환하고 실패 시 `false`를 반환한다.
     */
    suspend operator fun invoke(ownerId: UserId, userKeywordId: UserKeywordId): Boolean =
        userKeywordRepository.delete(ownerId, userKeywordId)
}
