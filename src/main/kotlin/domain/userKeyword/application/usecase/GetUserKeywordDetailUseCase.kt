package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.id.UserKeywordId
import com.peekr.common.util.AppLoggerFactory
import com.peekr.domain.userKeyword.application.dto.UserKeywordDetailDto
import com.peekr.domain.userKeyword.application.dto.toDto
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository

/**
 * 사용자 정보 상세 정보 조회
 *
 * @see invoke
 */
class GetUserKeywordDetailUseCase(private val userKeywordRepository: UserKeywordRepository) {
    /**
     * 사용자 정보 상세 정보를 조회한다.
     *
     * @param userKeywordId 사용자 키워드 ID
     * @param withUserInfo 사용자 정보 포함 여부
     *
     * @return [UserKeywordDetailDto] 사용자 키워드 상세 정보 DTO
     */
    suspend operator fun invoke(
        userKeywordId: Long,
        withUserInfo: Boolean,
    ): UserKeywordDetailDto? {
        val userKeywordIdVO = UserKeywordId(userKeywordId)
        val userKeywordDetailDto = userKeywordRepository
            .findUserKeywordDetail(userKeywordIdVO, withUserInfo)
            ?.toDto()
        return userKeywordDetailDto
    }
}

private val LOGGER = AppLoggerFactory.createLogger<GetUserKeywordDetailUseCase>()
