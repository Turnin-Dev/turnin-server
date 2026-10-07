package com.turnin.domain.pingPong.domain.message

/**
 * 핑퐁 알림 메시지
 */
object PingPongNotificationMessage {
    object Question {
        const val TITLE = "새 질문"

        fun message(questionerName: String) = "$questionerName 님이 질문을 남겼어요."
    }

    object Answer {
        const val TITLE = "새 답변"

        fun message(answererName: String) = "$answererName 님이 질문에 답변했어요."
    }
}
