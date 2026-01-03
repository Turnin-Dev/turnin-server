package com.peekr.domain.discover.application.usecase

import com.peekr.common.db.suspendTransaction
import com.peekr.common.model.id.UserId
import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.pagination.cursor.CursorPage
import com.peekr.domain.discover.application.dto.DiscoverContextDto
import com.peekr.domain.discover.application.dto.DiscoverKeywordDto
import com.peekr.domain.discover.application.dto.DiscoverUserDto
import com.peekr.domain.discover.domain.model.DiscoverContext
import com.peekr.domain.discover.domain.provider.KeywordProvider
import com.peekr.domain.discover.domain.provider.UserProvider
import com.peekr.domain.discover.domain.repository.DiscoverRepository
import com.peekr.domain.discover.exception.DiscoverException

/**
 * 탐색 컨텍스트 페이지네이션 조회
 *
 * @see invoke
 * @see DiscoverContext
 */
class GetDiscoverContextUseCase(
    private val discoverRepository: DiscoverRepository,
    private val userProvider: UserProvider,
    private val keywordProvider: KeywordProvider,
) {
    /**
     * 탐색 컨텍스트를 조회한다.
     *
     * [DiscoverContext]를 커서 기반 페이지네이션을 사용하여 조회한다.
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
    ): CursorPage<DiscoverContextDto> = suspendTransaction {
        // 0) VO 객체 변환
        val userIdVO = UserId(userId)

        // 1) 공유 키워드 정보 페이지네이션 조회
        val cursorPage = discoverRepository.getSharedKeywords(userIdVO, cursor, pageSize)
        if (cursorPage.items.isEmpty()) return@suspendTransaction CursorPage(emptyList(), null)

        // 2) 데이터 전처리
        val sharedKeywordInfos = cursorPage.items
        val userIds = sharedKeywordInfos.map { it.userId }
        val userKeywordIds = sharedKeywordInfos.map { it.userKeywordIds }
        val keywordIds = sharedKeywordInfos.map { it.keywordIds }

        if (userKeywordIds.flatten().size != keywordIds.flatten().size) {
            LOGGER.error("userKeywordIds and keywordIds size is not equal.")
            throw DiscoverException.KeywordIdPairingFailed()
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
        val discoverContextDtoList = cursorPage.items.map { sharedKeywordInfo ->
            val sUserId = sharedKeywordInfo.userId
            val foundedUser = userMap[sUserId] ?: run {
                LOGGER.error("User not found in map. userId: $sUserId")
                throw DiscoverException.UserNotFound()
            }
            val discoverUserDto = DiscoverUserDto(
                userId = sUserId,
                userName = foundedUser.userName.value,
                profileImageUrl = foundedUser.profileImageUrl,
            )

            val discoverKeywordDto = sharedKeywordInfo.userKeywordIds
                .zip(sharedKeywordInfo.keywordIds) { userKeywordId, keywordId ->
                    DiscoverKeywordDto(
                        userKeywordId = userKeywordId,
                        keywordId = keywordId,
                        keywordName = keywordMap[keywordId]?.value ?: run {
                            LOGGER.error("Keyword not found in map. keywordId: $keywordId")
                            throw DiscoverException.KeywordIdPairingFailed()
                        },
                    )
                }

            DiscoverContextDto(discoverUserDto, discoverKeywordDto)
        }

        // 6) 최종 반환
        CursorPage(discoverContextDtoList, cursorPage.nextCursor)
    }
}

private val LOGGER = AppLoggerFactory.createLogger<GetDiscoverContextUseCase>()
