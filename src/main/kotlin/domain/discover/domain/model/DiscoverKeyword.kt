package com.turnin.domain.discover.domain.model

import com.turnin.common.model.KeywordName
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserKeywordId

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
