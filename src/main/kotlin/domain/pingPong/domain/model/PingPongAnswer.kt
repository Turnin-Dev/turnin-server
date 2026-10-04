package com.turnin.domain.pingPong.domain.model

import com.turnin.common.model.id.PingPongAnswerId
import com.turnin.common.model.id.PingPongId

/**
 * 답변 모델
 *
 * @property id 답변 ID
 * @property pingPongId 답변이 달린 핑퐁 ID
 * @property answer 답변 내용
 * @property createdAt 생성 일자
 * @property updatedAt 수정 일자
 */
data class PingPongAnswer(
    val id: PingPongAnswerId,
    val pingPongId: PingPongId,
    val answer: PingPongContent,
    val createdAt: Long,
    val updatedAt: Long,
)
