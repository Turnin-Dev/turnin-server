package com.turnin.common.firebase

import com.turnin.common.model.NotificationType
import kotlin.test.assertEquals
import org.junit.Test

class FcmMessageTest {
    @Test
    fun `데이터 맵 변환 시 제목, 본문, 알림 유형과 추가 데이터를 함께 담는다`() {
        // given
        val message = FcmMessage(
            title = "새 답변",
            body = "홍길동 님이 질문에 답변했어요.",
            notiType = NotificationType.PING_PONG_ANSWER,
            data = mapOf("ref_owner_id" to "34"),
        )

        // when
        val result = message.toDataMap()

        // then
        val expected = mapOf(
            "title" to "새 답변",
            "body" to "홍길동 님이 질문에 답변했어요.",
            "noti_type" to "PING_PONG_ANSWER",
            "ref_owner_id" to "34",
        )
        assertEquals(expected, result)
    }

    @Test
    fun `추가 데이터에 제목, 본문, 알림 유형 키가 있어도 메시지의 기본 필드 값이 우선한다`() {
        // given
        val message = FcmMessage(
            title = "새 답변",
            body = "홍길동 님이 질문에 답변했어요.",
            notiType = NotificationType.PING_PONG_ANSWER,
            data = mapOf(
                "title" to "덮어쓴 제목",
                "body" to "덮어쓴 본문",
                "noti_type" to "NOTICE",
            ),
        )

        // when
        val result = message.toDataMap()

        // then
        val expected = mapOf(
            "title" to "새 답변",
            "body" to "홍길동 님이 질문에 답변했어요.",
            "noti_type" to "PING_PONG_ANSWER",
        )
        assertEquals(expected, result)
    }
}
