package com.peekr.domain.discover.presentation.dto

import com.peekr.domain.discover.application.dto.DiscoverKeywordDto
import kotlinx.serialization.Serializable

/**
 * 탐색용 키워드 응답 바디
 *
 * @property userKeywordId 사용자 키워드 ID
 * @property keywordId 키워드 ID
 * @property keywordName 키워드 명
 */
@Serializable
data class DiscoverKeywordResponse(
    val userKeywordId: Long,
    val keywordId: Long,
    val keywordName: String,
)

fun DiscoverKeywordDto.toResponse() =
    DiscoverKeywordResponse(
        userKeywordId = userKeywordId.value,
        keywordId = keywordId.value,
        keywordName = keywordName,
    )
