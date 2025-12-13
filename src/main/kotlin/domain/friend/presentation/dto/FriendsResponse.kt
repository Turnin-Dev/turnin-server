package com.peekr.domain.friend.presentation.dto

import com.peekr.domain.friend.application.dto.FriendsPagingDataDto
import kotlinx.serialization.Serializable

/**
 * 친구 목록 응답 바디
 *
 * @property pageNumber 현재 페이지 번호
 * @property pageSize 현재 페이지 크기
 * @property totalSize 전체 크기
 * @property hasNext 다음 페이지 존재 여부
 * @property friends 친구 목록
 */
@Serializable
data class FriendsResponse(
    val pageNumber: Long,
    val pageSize: Int,
    val totalSize: Long,
    val hasNext: Boolean,
    val friends: List<FriendInfoResponse>,
) {
    companion object {
        val sample = FriendsResponse(
            pageNumber = 1,
            pageSize = 10,
            totalSize = 100,
            hasNext = true,
            friends = listOf(FriendInfoResponse.sample),
        )
    }
}

fun FriendsPagingDataDto.toResponse(): FriendsResponse = FriendsResponse(
    pageNumber = pagingData.pageNumber,
    pageSize = pagingData.pageSize,
    totalSize = pagingData.totalSize,
    hasNext = pagingData.hasNext,
    friends = friends.map { it.toResponse() },
)
