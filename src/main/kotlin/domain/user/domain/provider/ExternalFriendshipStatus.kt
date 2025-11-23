package com.peekr.domain.user.domain.provider

/**
 * 외부에서 제공된 친구 관계 상태
 */
enum class ExternalFriendshipStatus {
    /** 아무 관계도 아닌 상태 */
    NOTHING,

    /** 친구 관계인 상태 */
    FRIENDS,

    /** 친구 요청 상태 */
    REQUESTED,

    /** 친구 요청을 받은 상태 */
    RECEIVED,
}
