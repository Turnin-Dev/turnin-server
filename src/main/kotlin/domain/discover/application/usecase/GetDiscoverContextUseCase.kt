package com.turnin.domain.discover.application.usecase

import com.turnin.common.db.suspendTransaction
import com.turnin.common.model.id.UserId
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.common.util.pagination.cursor.CursorCodec
import com.turnin.common.util.pagination.cursor.CursorPage
import com.turnin.domain.discover.application.dto.DiscoverContextDto
import com.turnin.domain.discover.application.dto.DiscoverCursorDto
import com.turnin.domain.discover.application.dto.DiscoverKeywordDto
import com.turnin.domain.discover.application.dto.DiscoverUserDto
import com.turnin.domain.discover.application.dto.toDomain
import com.turnin.domain.discover.domain.model.DiscoverContext
import com.turnin.domain.discover.domain.repository.DiscoverRepository

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
     * @param targetUserId 조회 대상 사용자 ID
     * @param viewerUserId 조회하려는 사용자 ID
     * @param cursorRaw 커서 값
     * @param pageSize 페이지 크기
     */
    suspend operator fun invoke(
        targetUserId: Long,
        viewerUserId: Long?,
        cursorRaw: String?,
        pageSize: Int,
    ): CursorPage<DiscoverContextDto, String> = suspendTransaction {
        // 0) 데이터 전처리
        val targetUserIdVO = UserId(targetUserId)
        val viewerUserIdVO = viewerUserId?.let { UserId(it) }

        // 커서 디코딩. 없으면 새 seed 발급 (첫 페이지), 있으면 기존 seed 재사용 (정렬 일관성 유지)
        val cursorDto = CursorCodec.decodeOrNull<DiscoverCursorDto>(cursorRaw)
        val seed = cursorDto?.seed ?: CursorCodec.newSeed()
        val domainCursor = cursorDto?.toDomain()

        // 1) 유사한 키워드를 가지고 있는 사용자 조회 (Native SQL)
        val resultsWithOneExtra = discoverRepository.findUserIdsWithSimilarKeywords(
            targetUserId = targetUserIdVO,
            viewerUserId = viewerUserIdVO,
            seed = seed,
            similarityThreshold = SIMILARITY_THRESHOLD,
            cursor = domainCursor,
            pageSize = pageSize + 1,
        )

        if (resultsWithOneExtra.isEmpty()) {
            return@suspendTransaction CursorPage(emptyList(), null)
        }

        // 2) 다음 페이지 존재 여부 확인 및 실제 반환할 결과 추출
        val hasNext = resultsWithOneExtra.size > pageSize
        val pageResults = if (hasNext) resultsWithOneExtra.take(pageSize) else resultsWithOneExtra
        val matchedUserIds = pageResults.map { it.userId }

        // 3) 조회된 사용자 ID 리스트를 통해 사용자 키워드 상세 정보 조회
        val sharedUserKeywords = discoverRepository.fetchSharedUserKeywords(matchedUserIds)

        // 4) 반환할 정보 매핑
        val sharedUserKeywordMap = sharedUserKeywords.groupBy { it.userId }
        val discoverContextDtoList = matchedUserIds.mapNotNull { matchedUserId ->
            val keywords = sharedUserKeywordMap[matchedUserId] ?: run {
                LOGGER.warn(
                    "SharedUserKeyword not found: " +
                        "matchedUserId=$matchedUserId, userKeywordIds=${sharedUserKeywords.map { it.userKeywordId }}",
                )
                return@mapNotNull null
            }
            val first = keywords.first()

            val discoverUserDto = DiscoverUserDto(
                id = matchedUserId,
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

        // 5) 다음 커서 결정 - seed와 마지막 결과의 정렬 키 3종을 함께 인코딩
        val nextCursor = if (hasNext) {
            val last = pageResults.last()
            CursorCodec.encode(
                DiscoverCursorDto(
                    seed = seed,
                    lastScore = last.matchScore,
                    lastShuffleKey = last.shuffleKey,
                    lastUserId = last.userId.value,
                ),
            )
        } else {
            null
        }

        // 6) 최종 반환
        CursorPage(discoverContextDtoList, nextCursor)
    }
}

private val LOGGER = AppLoggerFactory.createLogger<GetDiscoverContextUseCase>()

private const val SIMILARITY_THRESHOLD = 0.5
