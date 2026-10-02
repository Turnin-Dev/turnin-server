package com.turnin.domain.pingPong.domain.repository

import com.turnin.common.db.DatabaseException.DuplicatedDataException
import com.turnin.common.model.id.PingPongId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.pingPong.domain.model.PingPong
import com.turnin.domain.pingPong.domain.model.PingPongAnswer
import com.turnin.domain.pingPong.domain.model.PingPongContent

interface PingPongRepository {
    /**
     * 핑퐁(질문)을 생성한다.
     *
     * @param userKeywordId 질문을 등록할 사용자 키워드(게시물) ID
     * @param questionerId 질문자 ID
     * @param question 질문 내용
     *
     * @return 생성된 [PingPong]을 반환한다.
     */
    suspend fun create(
        userKeywordId: UserKeywordId,
        questionerId: UserId,
        question: PingPongContent,
    ): PingPong

    /**
     * 노출 중인 핑퐁(질문)을 조회한다.
     * (신고 누적으로 숨김 처리된 핑퐁 제외)
     *
     * @param pingPongId 핑퐁 ID
     *
     * @return 노출 중인 핑퐁이 있다면 [PingPong]을 반환하고, 없거나 숨김 처리되었다면 `null`을 반환한다.
     */
    suspend fun findVisibleById(pingPongId: PingPongId): PingPong?

    /**
     * 핑퐁 답변을 생성한다.
     *
     * @param pingPongId 답변을 등록할 핑퐁(질문) ID
     * @param answer 답변 내용
     *
     * @return 생성된 [PingPongAnswer]를 반환한다.
     *
     * @exception DuplicatedDataException 이미 답변이 등록된 질문인 경우
     */
    suspend fun createAnswer(
        pingPongId: PingPongId,
        answer: PingPongContent,
    ): PingPongAnswer
}
