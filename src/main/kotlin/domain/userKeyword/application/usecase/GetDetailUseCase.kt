package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.userKeyword.application.dto.UserKeywordDetailDto
import com.peekr.domain.userKeyword.application.dto.toDto
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository

/**
 * 사용자 키워드 ID로 사용자 키워드 상세 정보 조회
 *
 * @see invoke
 */
class GetDetailUseCase(private val userKeywordRepository: UserKeywordRepository) {
    /**
     * 사용자 키워드 ID로 사용자 키워드 상세 정보를 조회한다.
     *
     * @param userKeywordId 사용자 키워드 ID
     *
     * @return [UserKeywordDetailDto] 사용자 키워드 상세 정보 DTO
     */
    suspend operator fun invoke(
        currentUserId: Long,
        userKeywordId: Long,
    ): UserKeywordDetailDto? {
        val currentUserIdVO = UserId(currentUserId)
        val userKeywordIdVO = UserKeywordId(userKeywordId)
        val userKeywordDetailDto = userKeywordRepository
            .getDetailById(currentUserIdVO, userKeywordIdVO)
            ?.toDto()
        return userKeywordDetailDto
    }
}
