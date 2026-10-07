package com.turnin.domain.pingPong.presentation.dto

import com.turnin.domain.pingPong.application.dto.PingPongAnswerDto
import kotlinx.serialization.Serializable

/**
 * 답변 응답 바디
 *
 * @property id 답변 ID
 * @property pingPongId 답변이 달린 핑퐁 ID
 * @property answer 답변 내용
 * @property createdAt 생성 일자
 * @property updatedAt 수정 일자
 */
@Serializable
data class PingPongAnswerResponse(
    val id: Long,
    val pingPongId: Long,
    val answer: String,
    val createdAt: Long,
    val updatedAt: Long,
) {
    companion object {
        val sample = PingPongAnswerResponse(
            id = 1,
            pingPongId = 1,
            answer = "요즘 가장 자주 떠올리는 단어라서 골랐어요!",
            createdAt = 1697875200L,
            updatedAt = 1697875200L,
        )
    }
}

fun PingPongAnswerDto.toResponse() = PingPongAnswerResponse(
    id = id,
    pingPongId = pingPongId,
    answer = answer,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
