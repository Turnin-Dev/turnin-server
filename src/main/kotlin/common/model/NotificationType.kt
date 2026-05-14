package com.turnin.common.model

/** 알림 유형 */
enum class NotificationType {
    /** 친구 요청 */
    FRIEND_REQUEST,

    /** 친구 수락 */
    FRIEND_ACCEPT,

    /** 친구의 새 키워드 */
    NEW_KEYWORD,

    /** 공지사항 */
    NOTICE,

    /** 이벤트 */
    EVENT,

    ;

    val isBroadcast: Boolean
        get() = this == NOTICE || this == EVENT
}
