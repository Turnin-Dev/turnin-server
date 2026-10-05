package com.turnin.domain.pingPong.domain.model

import com.turnin.common.firebase.RefDataKey
import com.turnin.common.model.NotificationType
import com.turnin.common.model.id.UserId

/**
 * 핑퐁 알림 전송 요청 모델
 *
 * @property receiverId 수신자 ID
 * @property notiType 알림 유형
 * @property title 알림 제목
 * @property message 알림 본문
 * @property refId 딥링크용 참조 ID
 * @property refType 딥링크용 참조 타입
 * @property refData 딥링크용 부가 데이터
 */
data class PingPongNotificationCommand(
    val receiverId: UserId,
    val notiType: NotificationType,
    val title: String,
    val message: String,
    val refId: Long,
    val refType: String,
    val refData: Map<RefDataKey, String>,
)
