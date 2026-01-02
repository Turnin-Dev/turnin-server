package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.common.util.AppLoggerFactory
import com.peekr.domain.userKeyword.application.dto.UserKeywordDto
import com.peekr.domain.userKeyword.domain.provider.KeywordProvider
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import com.peekr.domain.userKeyword.exception.UserKeywordException

/**
 * 사용자 ID로 사용자별 키워드 리스트를 조회한다.
 *
 * 키워드 내용은 일부만 가져온다.
 *
 * @see invoke
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
    suspend operator fun invoke(userId: Long): List<UserKeywordDto> {
        // 0) 데이터 전처리
        val userIdVO = UserId(userId)

        // 1) 사용자 키워드 조회
        val userKeywords = userKeywordRepository.findListByUserId(userIdVO)
        if (userKeywords.isEmpty()) return emptyList()

        // 2) 키워드 명 조회 후 Map 생성
        val keywords = userKeywords.map { it.keywordId }
        val keywordMap = keywordProvider.findByIds(keywords).associateBy { it.id }
        if (keywordMap.size != keywords.size) {
            LOGGER.error("keyword not found: userId=$userId, keywords=$keywords, keywordMap=$keywordMap")
            throw UserKeywordException.NotExistsKeyword(null)
        }

        // 3) 사용자 키워드에 키워드 명 매핑
        return userKeywords.map { userKeyword ->
            val keywordName = keywordMap[userKeyword.keywordId]
                ?: run {
                    LOGGER.error("keyword not found: userId=$userId, keywordId=${userKeyword.keywordId}")
                    throw UserKeywordException.NotExistsKeyword(null)
                }

            UserKeywordDto(
                id = userKeyword.id.value,
                userId = userKeyword.userId.value,
                keywordId = userKeyword.keywordId.value,
                keywordName = keywordName.name,
                description = userKeyword.description.value,
                createdAt = userKeyword.createdAt,
                updatedAt = userKeyword.updatedAt,
            )
        }
    }
}

private val LOGGER = AppLoggerFactory.createLogger<GetUserKeywordsUseCase>()
