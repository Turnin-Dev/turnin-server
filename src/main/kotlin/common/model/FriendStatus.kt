package com.turnin.common.model

/**
 * 친구 상태
 *
 * 친구 추가/수정/삭제 요청이 요청자, 피요청자 관점 모두를 고려한 명확한 값으로 나타낸다.
 */
enum class FriendStatus {
    /** 아무 관계도 아닌 상태 */
    NOTHING,

    /** 친구 관계인 상태 */
    FRIENDS,

    /** 친구 요청 상태 */
    REQUESTED,

    /** 친구 요청을 받은 상태 */
    RECEIVED,
}
