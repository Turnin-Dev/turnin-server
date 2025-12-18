package com.peekr.domain.keywordGraph.domain.model

import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserKeywordId

/**
 * 키워드 노드 모델
 *
 * @property userKeywordId 사용자 키워드 ID
 * @property keywordId 키워드 ID
 * @property keywordName 키워드 명
 */
data class KeywordNode(
    val userKeywordId: UserKeywordId,
    val keywordId: KeywordId,
    val keywordName: String,
)
