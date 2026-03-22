package com.peekr.common.firebase

import com.peekr.common.model.NotificationType

/**
 * FCM 메시지 규격
 *
 * [token] 혹은 [topic] 중 하나는 필수이다.
 * 단, 여러 사용자에게 보내는 경우에는 둘 다 비어있을 수 있다.
 *
 * @property token 특정 사용자 대상
 * @property topic 브로드캐스트 대상
 * @property title 메시지 제목
 * @property body 메시지 본문
 * @property imageUrl 메시지에 첨부할 이미지 (예: 프로필 사진 등)
 * @property notiType 알림 유형
 * @property data 딥링크용 데이터 (ref_id, ref_type, user_id 등)
 */
data class FcmMessage(
    val token: String? = null,
    val topic: String? = null,
    val title: String,
    val body: String,
    val imageUrl: String? = null,
    val notiType: NotificationType,
    val data: Map<String, String> = emptyMap(),
) {
    /**
     * data-only 방식 전송을 위한 데이터 맵 변환
     * title, body, notiType 과 추가 data 를 포함한다.
     */
    fun toDataMap(): Map<String, String> = buildMap {
        put(FcmDataKey.TITLE, title)
        put(FcmDataKey.BODY, body)
        put(FcmDataKey.NOTI_TYPE, notiType.name)
        putAll(data)
    }
}
