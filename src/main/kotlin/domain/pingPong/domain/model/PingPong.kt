package com.turnin.domain.pingPong.domain.model

import com.turnin.common.model.id.PingPongId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId

/**
 * 핑퐁(질문) 모델
 *
 * @property id 핑퐁 ID
 * @property userKeywordId 질문이 달린 사용자 키워드(게시물) ID
 * @property questionerId 질문자 ID
 * @property question 질문 내용
 * @property createdAt 생성 일자
 * @property updatedAt 수정 일자
 */
data class PingPong(
    val id: PingPongId,
    val userKeywordId: UserKeywordId,
    val questionerId: UserId,
    val question: PingPongContent,
    val createdAt: Long,
    val updatedAt: Long,
)
