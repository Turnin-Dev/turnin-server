package com.peekr.domain.discover.application.usecase

import com.peekr.common.db.suspendTransaction
import com.peekr.common.model.id.UserId
import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.pagination.cursor.CursorPage
import com.peekr.domain.discover.application.dto.DiscoverContextDto
import com.peekr.domain.discover.application.dto.DiscoverKeywordDto
import com.peekr.domain.discover.application.dto.DiscoverUserDto
import com.peekr.domain.discover.domain.model.DiscoverContext
import com.peekr.domain.discover.domain.repository.DiscoverRepository
import com.peekr.domain.discover.exception.DiscoverException

/**
 * 탐색 컨텍스트 페이지네이션 조회
 *
 * @see invoke
 * @see DiscoverContext
 */
class GetDiscoverContextUseCase(private val discoverRepository: DiscoverRepository) {
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
    ): CursorPage<DiscoverContextDto, Long> = suspendTransaction {
        // 0) 데이터 전처리
        val userIdVO = UserId(userId)

        // 1) 유사한 키워드를 가지고 있는 사용자 ID 리스트를 조회 (Native SQL)
        val matchedUserIdsWithOneExtra = discoverRepository.findUserIdsWithSimilarKeywords(userIdVO, cursor, pageSize)

        if (matchedUserIdsWithOneExtra.isEmpty()) {
            return@suspendTransaction CursorPage(emptyList(), null)
        }

        // 2) 다음 페이지 존재 여부 확인 및 실제 반환할 ID 리스트 추출
        val hasNext = matchedUserIdsWithOneExtra.size > pageSize
        val matchedUserIds = if (hasNext) {
            matchedUserIdsWithOneExtra.take(pageSize)
        } else {
            matchedUserIdsWithOneExtra
        }

        // 3) 조회된 사용자 ID 리스트를 통해 사용자 키워드 상세 정보 조회
        val sharedUserKeywords = discoverRepository.fetchSharedUserKeywords(matchedUserIds)

        // 4) 반환할 정보 매핑
        val sharedUserKeywordMap = sharedUserKeywords.groupBy { it.userId }
        val discoverContextDtoList = matchedUserIds.map { targetUserId ->
            val keywords = sharedUserKeywordMap[targetUserId] ?: run {
                LOGGER.error(
                    "SharedUserKeyword not found: " +
                        "targetUserId=$targetUserId, userKeywordIds=${sharedUserKeywords.map { it.userKeywordId }}",
                )
                throw DiscoverException.KeywordIdPairingFailed()
            }
            val first = keywords.first()

            val discoverUserDto = DiscoverUserDto(
                id = targetUserId,
                name = first.userName.value,
                displayId = first.userDisplayId.value,
                profileImageUrl = first.userProfileImageUrl,
            )
            val discoverKeywordDtoList = keywords.map {
                DiscoverKeywordDto(
                    userKeywordId = it.userKeywordId,
                    keywordId = it.keywordId,
                    keywordName = it.keywordName.value,
                )
            }
            DiscoverContextDto(
                user = discoverUserDto,
                keywords = discoverKeywordDtoList,
            )
        }

        // 5) 다음 커서 결정
        val nextCursor = if (hasNext) matchedUserIds.last() else null

        // 6) 최종 반환
        CursorPage(discoverContextDtoList, nextCursor?.value)
    }
}

private val LOGGER = AppLoggerFactory.createLogger<GetDiscoverContextUseCase>()
