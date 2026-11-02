package com.turnin.domain.pingPong.domain.model

import com.turnin.common.model.id.PingPongId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId

/**
 * 핑퐁 모델
 *
 * 핑퐁은 사용자 키워드(게시물)에 남기는 질문 글이고, 답변은 [PingPongAnswer]로 따로 관리한다.
 * 표현 상황에 따라 질문과 답변의 묶음을 핑퐁이라 부를 수 있지만, 코드에서 `PingPong`은 질문 글만 뜻한다.
 * 질문과 답변을 함께 조회할 때는 [PingPongDetail]을 사용한다.
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
