package com.turnin.domain.pingPong.application.provider

import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.pingPong.domain.repository.PingPongRepository

/**
 * 외부에 제공할 PingPong 삭제 제공 API
 */
class PingPongDeletionSupportApi(private val pingPongRepository: PingPongRepository) {
    /**
     * 사용자와 관련된 핑퐁(사용자가 남긴 질문, 사용자의 게시물에 달린 핑퐁)을 전부 삭제한다.
     *
     * ###### 해당 메서드는 다른 사용자가 남긴 질문/답변도 함께 삭제하므로 주의해서 사용해야 한다.
     *
     * @param userId 사용자 ID
     */
    suspend fun deleteAllByUserId(userId: UserId) =
        pingPongRepository.deleteAllByUserId(userId)

    /**
     * 게시물(사용자 키워드)에 달린 핑퐁을 전부 삭제한다.
     *
     * @param userKeywordId 사용자 키워드(게시물) ID
     */
    suspend fun deleteAllByUserKeywordId(userKeywordId: UserKeywordId) =
        pingPongRepository.deleteAllByUserKeywordId(userKeywordId)
}
