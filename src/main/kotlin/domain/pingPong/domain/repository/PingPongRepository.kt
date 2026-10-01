package com.turnin.domain.pingPong.domain.repository

import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.pingPong.domain.model.PingPong
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
}
