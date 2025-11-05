package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.UserId
import com.peekr.common.model.UserKeywordId
import com.peekr.domain.userKeyword.application.dto.DescriptionDto
import com.peekr.domain.userKeyword.application.dto.toDto
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository

/**
 * 사용자 키워드 ID를 통해 사용자 키워드 설명을 조회한다.
 */
class GetDescriptionUseCase(private val userKeywordRepository: UserKeywordRepository) {
    /**
     * @param userKeywordId 사용자 키워드 ID
     *
     * @return [DescriptionDto] 사용자 키워드 설명을 반환한다. 설명이 존재하지 않는 경우 `null`을 반환한다.
     */
    suspend operator fun invoke(
        ownerId: UserId,
        userKeywordId: UserKeywordId,
    ): DescriptionDto? =
        userKeywordRepository.findDescriptionById(ownerId, userKeywordId)?.toDto()
}
