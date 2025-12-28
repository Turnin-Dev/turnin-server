package com.peekr.domain.keywordGraph.application.usecase

import com.peekr.common.db.suspendTransaction
import com.peekr.common.model.id.UserId
import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.pagination.cursor.CursorPage
import com.peekr.domain.keywordGraph.application.dto.KeywordNodeDto
import com.peekr.domain.keywordGraph.application.dto.NodeContextDto
import com.peekr.domain.keywordGraph.application.dto.UserNodeDto
import com.peekr.domain.keywordGraph.domain.model.NodeContext
import com.peekr.domain.keywordGraph.domain.provider.KeywordProvider
import com.peekr.domain.keywordGraph.domain.provider.UserProvider
import com.peekr.domain.keywordGraph.domain.repository.KeywordGraphRepository
import com.peekr.domain.keywordGraph.exception.KeywordGraphException

/**
 * 노드 컨텍스트 페이지네이션 조회
 *
 * @see invoke
 */
class GetNodeContextUseCase(
    private val keywordGraphRepository: KeywordGraphRepository,
    private val userProvider: UserProvider,
    private val keywordProvider: KeywordProvider,
) {
    /**
     * 노드 컨텍스트를 조회한다.
     *
     * [NodeContext]를 커서 기반 페이지네이션을 사용하여 조회한다.
     *
     * 단순 읽기(조회) 작업만 하는 로직이지만 일관성이 중요하므로 트랜잭션 처리가 되어있다.
     *
     * @param userId 조회할 사용자 ID
     * @param cursor 커서 값 (사용자 ID)
     * @param pageSize 페이지 사이즈
     */
    suspend operator fun invoke(
        userId: Long,
        cursor: Long?,
        pageSize: Int,
    ): CursorPage<NodeContextDto> = suspendTransaction {
        // 0) VO 객체 변환
        val userIdVO = UserId(userId)

        // 1) 공유 키워드 정보 페이지네이션 조회
        val cursorPage = keywordGraphRepository.getSharedKeywordInfos(userIdVO, cursor, pageSize)

        // 2) 데이터 전처리
        val sharedKeywordInfos = cursorPage.items
        val userIds = sharedKeywordInfos.map { it.userId }
        val userKeywordIds = sharedKeywordInfos.map { it.userKeywordIds }
        val keywordIds = sharedKeywordInfos.map { it.keywordIds }

        if (userKeywordIds.flatten().size != keywordIds.flatten().size) {
            LOGGER.error("userKeywordIds and keywordIds size is not equal.")
            throw KeywordGraphException.KeywordIdPairingFailed()
        }

        val keywordIdsSet = keywordIds.flatten().toSet()

        // 3) 사용자 정보 조회, 사용자 Map 생성
        val userMap = userProvider
            .findByIds(userIds)
            .associateBy { it.id }

        // 4) 키워드 ID Set 리스트를 통해 키워드 리스트 조회, 키워드 Map 생성
        val keywordMap = keywordProvider
            .findByIds(keywordIdsSet.toList())
            .associate { it.id to it.name }

        // 5) 각 노드 매핑, NodeContext 생성
        val nodeContexts = cursorPage.items.map { sharedKeywordInfo ->
            val sUserId = sharedKeywordInfo.userId
            val foundedUser = userMap[sUserId] ?: throw KeywordGraphException.UserNotFound()
            val userNode = UserNodeDto(
                userId = sUserId,
                userName = foundedUser.name.value,
                profileImageUrl = foundedUser.profileImageUrl,
            )

            val keywordNodes = sharedKeywordInfo.userKeywordIds
                .zip(sharedKeywordInfo.keywordIds) { userKeywordId, keywordId ->
                    KeywordNodeDto(
                        userKeywordId = userKeywordId,
                        keywordId = keywordId,
                        keywordName = keywordMap[keywordId]?.value
                            ?: throw KeywordGraphException.KeywordIdPairingFailed(),
                    )
                }

            NodeContextDto(userNode, keywordNodes)
        }

        // 6) 최종 반환
        CursorPage(nodeContexts, cursorPage.nextCursor)
    }
}

private val LOGGER = AppLoggerFactory.createLogger<GetNodeContextUseCase>()
