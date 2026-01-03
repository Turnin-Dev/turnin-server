package com.peekr.domain.discover.domain.model

import com.peekr.common.model.KeywordName
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserKeywordId

/**
 * 탐색용 키워드 모델
 *
 * @property userKeywordId 사용자 키워드 ID
 * @property keywordId 키워드 ID
 * @property keywordName 키워드 명
 */
data class DiscoverKeyword(
    val userKeywordId: UserKeywordId,
    val keywordId: KeywordId,
    val keywordName: KeywordName,
)
