package com.peekr.domain.keywordGraph.domain.model

import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId

/**
 * 같은 키워드를 공유하고 있는 사용자 ID, 사용자 키워드 ID, 키워드 ID 목록이 담겨있다.
 */
data class SharedKeywordInfo(
    val userId: UserId,
    val userKeywordIds: List<UserKeywordId>,
    val keywordIds: List<KeywordId>,
)
