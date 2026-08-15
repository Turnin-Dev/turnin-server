package com.turnin.domain.discover.application.dto

import com.turnin.domain.discover.domain.model.DiscoverCursor
import kotlinx.serialization.Serializable

/**
 * 탐색 커서 (다음 페이지 조회를 위한 마지막 항목의 정렬 기준값)
 */
@Serializable
data class DiscoverCursorDto(
    val seed: String,
    val lastScore: Double,
    val lastShuffleKey: Int,
    val lastUserId: Long,
)

fun DiscoverCursorDto.toDomain(): DiscoverCursor =
    DiscoverCursor(
        lastScore = lastScore,
        lastShuffleKey = lastShuffleKey,
        lastUserId = lastUserId,
    )
