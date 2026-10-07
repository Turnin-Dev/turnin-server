package com.turnin.common.model

/** 알림 유형 */
enum class NotificationType {
    /** 친구 요청 */
    FRIEND_REQUEST,

    /** 친구 수락 */
    FRIEND_ACCEPT,

    /** 친구의 새 키워드 */
    NEW_KEYWORD,

    /** 핑퐁 질문 등록 (게시물 작성자에게) */
    PING_PONG_QUESTION,

    /** 핑퐁 답변 등록 (질문자에게) */
    PING_PONG_ANSWER,

    /** 공지사항 */
    NOTICE,

    /** 이벤트 */
    EVENT,

    ;

    val isBroadcast: Boolean
        get() = this == NOTICE || this == EVENT
}
