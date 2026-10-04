package com.turnin.domain.pingPong.application.dto

import com.turnin.domain.pingPong.domain.model.PingPongAnswer

/**
 * 답변 DTO
 *
 * @property id 답변 ID
 * @property pingPongId 답변이 달린 핑퐁 ID
 * @property answer 답변 내용
 * @property createdAt 생성 일자
 * @property updatedAt 수정 일자
 */
data class PingPongAnswerDto(
    val id: Long,
    val pingPongId: Long,
    val answer: String,
    val createdAt: Long,
    val updatedAt: Long,
)

fun PingPongAnswer.toDto(): PingPongAnswerDto = PingPongAnswerDto(
    id = id.value,
    pingPongId = pingPongId.value,
    answer = answer.value,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
