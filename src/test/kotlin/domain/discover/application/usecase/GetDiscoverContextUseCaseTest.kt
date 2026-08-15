package com.turnin.domain.discover.application.usecase

import com.turnin.common.model.KeywordName
import com.turnin.common.model.UserName
import com.turnin.common.model.id.DisplayId
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.common.util.pagination.cursor.CursorCodec
import com.turnin.domain.discover.application.dto.DiscoverCursorDto
import com.turnin.domain.discover.domain.model.DiscoverCursor
import com.turnin.domain.discover.domain.model.SharedUserKeyword
import com.turnin.domain.discover.domain.repository.DiscoverRepository
import com.turnin.domain.discover.domain.repository.DiscoveredResult
import com.turnin.util.db.TestDatabaseFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetDiscoverContextUseCaseTest {
    private val discoverRepository: DiscoverRepository = mockk()
    private val usecase = GetDiscoverContextUseCase(discoverRepository)

    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @After
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `DiscoverContext 페이지네이션 조회 성공 테스트`() = runTest {
        // given: 3명의 사용자, 사용자 당 3개의 키워드 준비
        val targetUserId = UserId(1L)
        val viewerUserId = UserId(1L)
        val keywordCount = 3
        val userCount = 3
        val pageSize = 2

        val sharedUserKeywords = List(userCount) { userIdx ->
            val userId = userIdx + 1L + 1L
            List(keywordCount) { keywordIdx ->
                val keywordId = keywordIdx + 1L
                val userKeywordId = (userId * 10) + keywordId
                createSharedUserKeyword(userId, userKeywordId, keywordId)
            }
        }.flatten()

        // findUserIdsWithSimilarKeywords 조회는 실제로 (pageSize + 1)개가 조회되기 때문에 3명이 조회되었다고 가정
        val matchedResults = listOf(
            createDiscoveredResult(2L, matchScore = 0.9, shuffleKey = 300),
            createDiscoveredResult(3L, matchScore = 0.8, shuffleKey = 200),
            createDiscoveredResult(4L, matchScore = 0.7, shuffleKey = 100),
        )
        coEvery {
            discoverRepository.findUserIdsWithSimilarKeywords(
                targetUserId = targetUserId,
                viewerUserId = viewerUserId,
                seed = any(),
                similarityThreshold = any(),
                cursor = null,
                pageSize = pageSize + 1,
            )
        } returns matchedResults

        // fetchSharedUserKeywords는 2, 3번 유저의 키워드만 요청받음 (pageSize가 2기 때문에)
        val requestedIds = listOf(UserId(2L), UserId(3L))
        coEvery {
            discoverRepository.fetchSharedUserKeywords(any())
        } returns sharedUserKeywords.filter { it.userId in requestedIds }

        // when
        val result = usecase(targetUserId.value, viewerUserId.value, null, pageSize)

        // then
        coVerify(exactly = 1) {
            discoverRepository.findUserIdsWithSimilarKeywords(
                targetUserId = targetUserId,
                viewerUserId = viewerUserId,
                seed = any(),
                similarityThreshold = any(),
                cursor = null,
                pageSize = pageSize + 1,
            )
        }

        // 페이지네이션 크기 검증
        assertEquals(pageSize, result.items.size)

        // 데이터 순서 검증 (match_score/shuffle_key 순서를 따라 2, 3, 4 중 2, 3이 유지되어야 함)
        assertEquals(
            2L,
            result.items[0]
                .user.id.value,
        )
        assertEquals(
            3L,
            result.items[1]
                .user.id.value,
        )

        // 키워드 그룹핑 검증
        assertEquals(keywordCount, result.items[0].keywords.size)
        assertEquals(keywordCount, result.items[1].keywords.size)

        // 다음 커서 존재 여부 및 내용 검증 (평문 비교가 아니라 디코딩해서 확인)
        assertNotNull(result.nextCursor, "데이터가 더 남아있으므로 nextCursor가 존재해야 한다.")
        val decodedCursor = CursorCodec.decodeOrNull<DiscoverCursorDto>(result.nextCursor)
        assertNotNull(decodedCursor)
        assertEquals(3L, decodedCursor.lastUserId, "nextCursor는 현재 페이지의 마지막 결과(3L) 정보를 담아야 한다.")
        assertEquals(0.8, decodedCursor.lastScore)
        assertEquals(200, decodedCursor.lastShuffleKey)
    }

    @Test
    fun `커서가 주어지면 디코딩된 seed와 정렬 키가 리포지토리에 그대로 전달된다`() = runTest {
        // given
        val targetUserId = UserId(1L)
        val pageSize = 2
        val existingCursorDto = DiscoverCursorDto(
            seed = "fixed-seed",
            lastScore = 0.95,
            lastShuffleKey = 500,
            lastUserId = 10L,
        )
        val cursorRaw = CursorCodec.encode(existingCursorDto)

        val expectedDomainCursor = DiscoverCursor(
            lastScore = 0.95,
            lastShuffleKey = 500,
            lastUserId = 10L,
        )

        coEvery {
            discoverRepository.findUserIdsWithSimilarKeywords(
                targetUserId = targetUserId,
                viewerUserId = null,
                seed = "fixed-seed",
                similarityThreshold = any(),
                cursor = expectedDomainCursor,
                pageSize = pageSize + 1,
            )
        } returns emptyList()

        // when
        val result = usecase(targetUserId.value, null, cursorRaw, pageSize)

        // then: 커서에 담긴 seed가 그대로 재사용되었는지 검증 (셔플 정렬 일관성 유지를 위해 필수)
        coVerify(exactly = 1) {
            discoverRepository.findUserIdsWithSimilarKeywords(
                targetUserId = targetUserId,
                viewerUserId = null,
                seed = "fixed-seed",
                similarityThreshold = any(),
                cursor = expectedDomainCursor,
                pageSize = pageSize + 1,
            )
        }
        assertTrue(result.items.isEmpty())
    }

    @Test
    fun `마지막 페이지인 경우 nextCursor가 null이다`() = runTest {
        val targetUserId = UserId(1L)
        val pageSize = 3

        // pageSize + 1보다 적은 결과 -> 마지막 페이지
        val matchedResults = listOf(
            createDiscoveredResult(2L, matchScore = 0.9, shuffleKey = 300),
            createDiscoveredResult(3L, matchScore = 0.8, shuffleKey = 200),
        )
        coEvery {
            discoverRepository.findUserIdsWithSimilarKeywords(
                targetUserId = targetUserId,
                viewerUserId = null,
                seed = any(),
                similarityThreshold = any(),
                cursor = null,
                pageSize = pageSize + 1,
            )
        } returns matchedResults

        coEvery {
            discoverRepository.fetchSharedUserKeywords(any())
        } returns matchedResults.map {
            createSharedUserKeyword(it.userId.value, it.userId.value * 10 + 1, 1L)
        }

        // when
        val result = usecase(targetUserId.value, null, null, pageSize)

        // then
        assertEquals(2, result.items.size)
        assertNull(result.nextCursor)
    }

    @Test
    fun `조회 결과가 없으면 빈 페이지를 반환한다`() = runTest {
        val targetUserId = UserId(1L)
        val pageSize = 10

        coEvery {
            discoverRepository.findUserIdsWithSimilarKeywords(
                targetUserId = targetUserId,
                viewerUserId = null,
                seed = any(),
                similarityThreshold = any(),
                cursor = null,
                pageSize = pageSize + 1,
            )
        } returns emptyList()

        // when
        val result = usecase(targetUserId.value, null, null, pageSize)

        // then
        assertTrue(result.items.isEmpty())
        assertNull(result.nextCursor)
        coVerify(exactly = 0) { discoverRepository.fetchSharedUserKeywords(any()) }
    }

    @Test
    fun `1, 2단계 쿼리 사이 레이스 컨디션으로 키워드가 사라진 유저는 결과에서 스킵된다`() = runTest {
        // given
        val targetUserId = UserId(1L)
        val pageSize = 3

        val matchedResults = listOf(
            createDiscoveredResult(2L, matchScore = 0.9, shuffleKey = 300),
            createDiscoveredResult(3L, matchScore = 0.8, shuffleKey = 200),
        )
        coEvery {
            discoverRepository.findUserIdsWithSimilarKeywords(
                targetUserId = targetUserId,
                viewerUserId = null,
                seed = any(),
                similarityThreshold = any(),
                cursor = null,
                pageSize = pageSize + 1,
            )
        } returns matchedResults

        // 2번 유저의 키워드만 반환 (3번 유저는 그 사이 삭제된 것으로 가정)
        coEvery {
            discoverRepository.fetchSharedUserKeywords(any())
        } returns listOf(createSharedUserKeyword(2L, 21L, 1L))

        // when
        val result = usecase(targetUserId.value, null, null, pageSize)

        // then: 예외 없이 3번 유저만 빠지고 2번 유저만 반환됨
        assertEquals(1, result.items.size)
        assertEquals(
            2L,
            result.items[0]
                .user.id.value,
        )
    }

    companion object {
        private fun createSharedUserKeyword(
            userId: Long,
            userKeywordId: Long,
            keywordId: Long,
        ) = SharedUserKeyword(
            userId = UserId(userId),
            userName = UserName("user"),
            userDisplayId = DisplayId("did"),
            userProfileImageUrl = null,
            userKeywordId = UserKeywordId(userKeywordId),
            keywordId = KeywordId(keywordId),
            keywordName = KeywordName("keyword"),
        )

        private fun createDiscoveredResult(
            userId: Long,
            matchScore: Double,
            shuffleKey: Int,
        ) = DiscoveredResult(
            userId = UserId(userId),
            matchScore = matchScore,
            shuffleKey = shuffleKey,
        )
    }
}
