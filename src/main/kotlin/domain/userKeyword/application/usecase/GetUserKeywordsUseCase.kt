package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.userKeyword.application.dto.UserKeywordDto
import com.peekr.domain.userKeyword.application.dto.toDto
import com.peekr.domain.userKeyword.domain.provider.KeywordProvider
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import com.peekr.domain.userKeyword.exception.UserKeywordException

/**
 * 사용자 ID로 사용자별 키워드 리스트를 조회한다.
 */
class GetUserKeywordsUseCase(
    private val userKeywordRepository: UserKeywordRepository,
    private val keywordProvider: KeywordProvider,
) {
    /**
     * @param userId 사용자 ID
     *
     * @return [UserKeywordDto] 리스트를 반환한다.
     */
    suspend operator fun invoke(userId: UserId): List<UserKeywordDto> =
        userKeywordRepository.findByUserId(userId).map { userKeyword ->
            val keyword = keywordProvider.findById(userKeyword.keywordId)
            if (keyword != null) {
                userKeyword.toDto(keyword.name)
            } else {
                throw UserKeywordException.NotExistsKeyword(null)
            }
        }
}
