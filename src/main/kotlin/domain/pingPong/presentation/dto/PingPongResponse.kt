package com.turnin.domain.pingPong.presentation.dto

import com.turnin.domain.pingPong.application.dto.PingPongDto
import kotlinx.serialization.Serializable

/**
 * 핑퐁(질문) 응답 바디
 *
 * @property id 핑퐁 ID
 * @property userKeywordId 질문이 달린 사용자 키워드(게시물) ID
 * @property questionerId 질문자 ID
 * @property question 질문 내용
 * @property createdAt 생성 일자
 * @property updatedAt 수정 일자
 */
@Serializable
data class PingPongResponse(
    val id: Long,
    val userKeywordId: Long,
    val questionerId: Long,
    val question: String,
    val createdAt: Long,
    val updatedAt: Long,
) {
    companion object {
        val sample = PingPongResponse(
            id = 1,
            userKeywordId = 1,
            questionerId = 2,
            question = "이 키워드를 고른 이유가 궁금해요!",
            createdAt = 1697875200L,
            updatedAt = 1697875200L,
        )
    }
}

fun PingPongDto.toResponse() = PingPongResponse(
    id = id,
    userKeywordId = userKeywordId,
    questionerId = questionerId,
    question = question,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
