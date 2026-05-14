package com.turnin.domain.userKeyword.application.usecase

import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.userKeyword.application.dto.UserKeywordDetailDto
import com.turnin.domain.userKeyword.application.dto.toDto
import com.turnin.domain.userKeyword.domain.repository.UserKeywordRepository

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
