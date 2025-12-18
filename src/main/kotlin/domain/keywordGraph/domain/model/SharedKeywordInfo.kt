package com.peekr.domain.keywordGraph.domain.model

import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId

/**
 * 같은 키워드를 공유하고 있는 사용자 ID와 키워드 ID 목록이 담겨있다.
 */
data class SharedKeywordInfo(
    val userId: UserId,
    val keywordIds: List<KeywordId>,
)
