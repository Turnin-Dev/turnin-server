package com.turnin.domain.pingPong.domain.repository

import com.turnin.common.db.DatabaseException.DuplicatedDataException
import com.turnin.common.model.id.PingPongId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.pingPong.domain.model.PingPong
import com.turnin.domain.pingPong.domain.model.PingPongAnswer
import com.turnin.domain.pingPong.domain.model.PingPongContent
import com.turnin.domain.pingPong.domain.model.PingPongDetail

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
     * 게시물(사용자 키워드)에 달린 노출 중인 핑퐁 목록을 최신순으로 조회한다. (커서 기반 페이지네이션)
     * (숨김 처리된 질문, 비활성화 질문자의 핑퐁 제외)
     *
     * 답변이 숨김 처리된 경우 질문은 노출하고 답변은 `null`로 반환한다.
     *
     * @param currentUserId 현재 조회 요청한 사용자 ID
     * @param userKeywordId 사용자 키워드(게시물) ID
     * @param cursor 커서 값 (핑퐁 ID, 해당 ID보다 작은 핑퐁부터 조회), null 이면 첫 페이지
     * @param size 조회할 개수
     * @param excludeBlockedQuestioners `true`면 조회자와 차단 관계(양방향)인 질문자의 핑퐁을 제외한다.
     *
     * @return [PingPongDetail] 목록 (최신순)
     */
    suspend fun findVisibleDetailsByUserKeywordId(
        currentUserId: UserId,
        userKeywordId: UserKeywordId,
        cursor: PingPongId?,
        size: Int,
        excludeBlockedQuestioners: Boolean,
    ): List<PingPongDetail>

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
