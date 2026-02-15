package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.userKeyword.application.dto.UserKeywordDetailDto
import com.peekr.domain.userKeyword.application.dto.toDto
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository

/**
 * 사용자 ID로 사용자의 키워드 상세 정보 리스트 조회
 *
 * @see invoke
 */
class GetDetailsUseCase(private val userKeywordRepository: UserKeywordRepository) {
    /**
     * 사용자 ID로 사용자의 키워드 상세 정보 리스트를 조회한다.
     *
     * 현재 사용자 키워드 개수 제한이 있기 때문에 페이지네이션은 적용되지 않은 상태이다.
     *
     * @param userId 사용자 ID
     *
     * @return [UserKeywordDetailDto] 사용자 키워드 상세 정보 DTO
     */
    suspend operator fun invoke(
        currentUserId: Long,
        userId: Long,
    ): List<UserKeywordDetailDto> {
        val currentUserIdVO = UserId(currentUserId)
        val userIdVO = UserId(userId)
        val userKeywordDetailDtoList = userKeywordRepository
            .getDetailsByUserId(currentUserIdVO, userIdVO)
            .map { it.toDto() }
        return userKeywordDetailDtoList
    }
}
