package com.peekr.domain.friend.domain.model

/**
 * 페이지네이션용 친구 목록
 *
 * @property totalSize 전체 크기
 * @property friends 친구 목록
 */
data class FriendsPagingData(
    val totalSize: Long,
    val friends: List<Friend>,
)
