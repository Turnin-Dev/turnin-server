package com.peekr.domain.keywordGraph.presentation.dto

import com.peekr.domain.keywordGraph.application.dto.KeywordNodeDto
import kotlinx.serialization.Serializable

/**
 * 키워드 노드 모델 응답 바디
 *
 * @property userKeywordId 사용자 키워드 ID
 * @property keywordId 키워드 ID
 * @property keywordName 키워드 명
 */
@Serializable
data class KeywordNodeResponse(
    val userKeywordId: Long,
    val keywordId: Long,
    val keywordName: String,
)

fun KeywordNodeDto.toResponse() =
    KeywordNodeResponse(
        userKeywordId = userKeywordId.value,
        keywordId = keywordId.value,
        keywordName = keywordName,
    )
