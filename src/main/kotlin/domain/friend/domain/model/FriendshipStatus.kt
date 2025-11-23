package com.peekr.domain.friend.domain.model

/**
 * 친구 관계 상태
 *
 * [com.peekr.common.model.FriendStatus]와는 다르게 좀 더 자세한 상태이다.
 *
 * 친구 추가/수정/삭제 요청이 요청자, 피요청자 관점 모두를 고려한 명확한 값으로 나타낸다.
 */
enum class FriendshipStatus {
    /** 아무 관계도 아닌 상태 */
    NOTHING,

    /** 친구 관계인 상태 */
    FRIENDS,

    /** 친구 요청 상태 */
    REQUESTED,

    /** 친구 요청을 받은 상태 */
    RECEIVED,
}
