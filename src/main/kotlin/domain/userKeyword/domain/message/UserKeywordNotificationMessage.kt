package com.peekr.domain.userKeyword.domain.message

/**
 * 키워드 알림 메시지
 */
object KeywordNotificationMessage {
    const val TITLE = "새 키워드"

    /**
     * 새 키워드 등록 알림 메시지
     *
     * @param senderName 키워드를 등록한 사용자 이름
     */
    fun message(senderName: String) = "${senderName}님이 새 키워드를 등록했어요."
}
