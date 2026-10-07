package com.turnin.domain.pingPong.application.usecase

import com.turnin.common.model.id.PingPongId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.common.util.pagination.cursor.CursorPage
import com.turnin.domain.pingPong.application.dto.PingPongDetailDto
import com.turnin.domain.pingPong.application.dto.toDto
import com.turnin.domain.pingPong.domain.provider.UserKeywordProvider
import com.turnin.domain.pingPong.domain.repository.PingPongRepository
import com.turnin.domain.pingPong.exception.PingPongException

/**
 * 핑퐁 목록 조회
 *
 * 게시물(사용자 키워드)에 달린 핑퐁(질문 + 답변) 목록을 최신순으로 조회한다. (커서 기반 페이지네이션)
 *
 * 숨김 처리된 질문, 비활성화 질문자, 조회자와 차단 관계인 질문자의 핑퐁은 제외하며,
 * 답변이 숨김 처리된 경우 질문만 노출한다.
 *
 * 단, 게시물 작성자가 조회하는 경우에는 차단 관계인 질문자의 핑퐁도 노출한다.
 * (차단 전에 달린 질문을 작성자가 삭제/신고할 수 있도록 하기 위함)
 *
 * @throws [PingPongException.UserKeywordNotFound] 게시물이 없거나, 비활성화/차단 관계로 조회할 수 없는 경우
 */
class GetPingPongsUseCase(
    private val pingPongRepository: PingPongRepository,
    private val userKeywordProvider: UserKeywordProvider,
) {
    /**
     * @param currentUserId 현재 조회 요청한 사용자 ID
     * @param userKeywordId 조회할 사용자 키워드(게시물) ID
     * @param cursor 커서 값 (핑퐁 ID), null 이면 첫 페이지
     * @param pageSize 페이지 크기
     *
     * @return [CursorPage]
     */
    suspend operator fun invoke(
        currentUserId: UserId,
        userKeywordId: Long,
        cursor: Long?,
        pageSize: Int,
    ): CursorPage<PingPongDetailDto, Long> {
        val userKeywordIdVO = UserKeywordId(userKeywordId)

        val ownerId = userKeywordProvider.findOwnerId(currentUserId, userKeywordIdVO)
            ?: throw PingPongException.UserKeywordNotFound()

        // 1. size + 1 개 조회
        val pingPongsWithOneExtra = pingPongRepository.findVisibleDetailsByUserKeywordId(
            currentUserId = currentUserId,
            userKeywordId = userKeywordIdVO,
            cursor = cursor?.let { PingPongId(it) },
            size = pageSize + 1,
            excludeBlockedQuestioners = ownerId != currentUserId,
        )

        // 2. 다음 페이지 존재 여부 확인 및 다음 커서 결정
        val hasNext = pingPongsWithOneExtra.size > pageSize
        val pingPongs = if (hasNext) {
            pingPongsWithOneExtra.take(pageSize)
        } else {
            pingPongsWithOneExtra
        }
        val nextCursor = if (hasNext) pingPongs.last().pingPong.id.value else null

        // 3. 결과 반환
        return CursorPage(
            items = pingPongs.map { it.toDto() },
            nextCursor = nextCursor,
        )
    }
}
