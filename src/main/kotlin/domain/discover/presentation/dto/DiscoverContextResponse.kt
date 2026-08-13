package com.turnin.domain.discover.presentation.dto

import com.turnin.common.util.pagination.cursor.CursorPage
import com.turnin.domain.discover.application.dto.DiscoverContextDto
import kotlinx.serialization.Serializable

/**
 * NodeContext 응답 바디
 *
 * @property user 사용자 노드 응답 바디
 * @property keywords 키워드 노드 응답 바디 리스트
 */
@Serializable
data class DiscoverContextResponse(
    val user: DiscoverUserResponse,
    val keywords: List<DiscoverKeywordResponse>,
) {
    companion object {
        val sample = CursorPage(
            items = List(2) {
                DiscoverContextResponse(
                    user = DiscoverUserResponse(
                        id = (it + 1).toLong(),
                        name = "name",
                        displayId = "displayId",
                        profileImageUrl = "https://image-server-1.com/image$it.jpg",
                    ),
                    keywords = listOf(
                        DiscoverKeywordResponse(
                            userKeywordId = (it + 1).toLong(),
                            keywordId = (it + 1).toLong(),
                            keywordName = "Keyword ${(it + 1).toLong()}",
                        ),
                    ),
                )
            },
            nextCursor = "encoded-cursor-value",
        )
    }
}

fun DiscoverContextDto.toResponse() =
    DiscoverContextResponse(
        user = user.toResponse(),
        keywords = keywords.map { it.toResponse() },
    )
