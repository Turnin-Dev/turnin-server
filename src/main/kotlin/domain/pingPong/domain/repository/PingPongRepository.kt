package com.turnin.domain.pingPong.domain.repository

import com.turnin.common.db.DatabaseException.DuplicatedDataException
import com.turnin.common.model.id.PingPongAnswerId
import com.turnin.common.model.id.PingPongId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.pingPong.domain.model.PingPong
import com.turnin.domain.pingPong.domain.model.PingPongAnswer
import com.turnin.domain.pingPong.domain.model.PingPongAnswerWithAnswerer
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

    /**
     * 핑퐁(질문)에 달린 노출 중인 답변을 조회한다.
     * (신고 누적으로 숨김 처리된 답변 제외)
     *
     * @param pingPongId 핑퐁(질문) ID
     *
     * @return 노출 중인 답변이 있다면 [PingPongAnswer]를 반환하고, 없거나 숨김 처리되었다면 `null`을 반환한다.
     */
    suspend fun findVisibleAnswerByPingPongId(pingPongId: PingPongId): PingPongAnswer?

    /**
     * 노출 중인 답변을 답변자 정보와 함께 조회한다.
     * (답변 또는 답변이 달린 질문이 신고 누적으로 숨김 처리된 경우 제외)
     *
     * @param pingPongAnswerId 핑퐁 답변 ID
     *
     * @return 노출 중인 답변이 있다면 [PingPongAnswerWithAnswerer]를 반환하고, 없거나 숨김 처리되었다면 `null`을 반환한다.
     */
    suspend fun findVisibleAnswerWithAnswererById(pingPongAnswerId: PingPongAnswerId): PingPongAnswerWithAnswerer?

    /**
     * 핑퐁(질문)을 삭제한다. (연결된 답변은 CASCADE로 함께 삭제된다)
     *
     * @param pingPongId 핑퐁 ID
     *
     * @return 삭제되었다면 `true`, 삭제할 핑퐁이 없다면 `false`를 반환한다.
     */
    suspend fun delete(pingPongId: PingPongId): Boolean

    /**
     * 핑퐁 답변을 삭제한다.
     *
     * @param pingPongAnswerId 핑퐁 답변 ID
     *
     * @return 삭제되었다면 `true`, 삭제할 답변이 없다면 `false`를 반환한다.
     */
    suspend fun deleteAnswer(pingPongAnswerId: PingPongAnswerId): Boolean

    /**
     * 핑퐁(질문)을 숨김 처리한다. (이미 숨김 처리된 경우 숨김 시각을 갱신하지 않는다)
     *
     * @param pingPongId 핑퐁 ID
     *
     * @return 이번 요청으로 숨김 처리되었다면 `true`, 핑퐁이 없거나 이미 숨김 처리되었다면 `false`를 반환한다.
     */
    suspend fun hide(pingPongId: PingPongId): Boolean

    /**
     * 핑퐁 답변을 숨김 처리한다. (이미 숨김 처리된 경우 숨김 시각을 갱신하지 않는다)
     *
     * @param pingPongAnswerId 핑퐁 답변 ID
     *
     * @return 이번 요청으로 숨김 처리되었다면 `true`, 답변이 없거나 이미 숨김 처리되었다면 `false`를 반환한다.
     */
    suspend fun hideAnswer(pingPongAnswerId: PingPongAnswerId): Boolean
}
