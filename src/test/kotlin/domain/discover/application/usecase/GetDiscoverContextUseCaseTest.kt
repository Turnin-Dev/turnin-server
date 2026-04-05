package com.peekr.domain.discover.application.usecase

import com.peekr.common.model.KeywordName
import com.peekr.common.model.UserName
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.discover.domain.model.SharedUserKeyword
import com.peekr.domain.discover.domain.repository.DiscoverRepository
import com.peekr.util.db.TestDatabaseFactory
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
        val keywordCount = 3 // 추가할 키워드 개수
        val userCount = 3 // 추가할 사용자 개수 (사용자 ID는 2부터 시작)
        val pageSize = 2

        val sharedUserKeywords = List(userCount) { userIdx ->
            val userId = userIdx + 1L + 1L // 타겟 사용자 ID 제외하려면 2L 부터 생성해야 함
            List(keywordCount) { keywordIdx ->
                val keywordId = keywordIdx + 1L
                val userKeywordId = (userId * 10) + keywordId // 사용자 키워드 ID는 임의 생성
                createSharedUserKeyword(userId, userKeywordId, keywordId)
            }
        }.flatten()

        // findUserIdsWithSimilarKeywords 조회는 실제로 (pageSize + 1)개가 조회되기 때문에 3명이 조회되었다고 가정
        val matchedUserIds = listOf(UserId(2L), UserId(3L), UserId(4L))
        coEvery {
            discoverRepository.findUserIdsWithSimilarKeywords(
                targetUserId = targetUserId,
                cursor = null,
                pageSize = pageSize + 1,
            )
        } returns matchedUserIds

        // fetchSharedUserKeywords는 2, 3번 유저의 키워드만 요청받음 (pageSize가 2기 때문에)
        val requestedIds = listOf(UserId(2L), UserId(3L))
        coEvery {
            discoverRepository.fetchSharedUserKeywords(any())
        } returns sharedUserKeywords.filter { it.userId in requestedIds }

        // when
        val result = usecase(targetUserId.value, null, pageSize)

        // then
        coVerify(exactly = 1) {
            discoverRepository.findUserIdsWithSimilarKeywords(
                targetUserId = targetUserId,
                cursor = null,
                pageSize = pageSize + 1,
            )
        }

        // 페이지네이션 크기 검증
        assertEquals(pageSize, result.items.size)

        // 데이터 순서 검증 (2, 3, 4) 순서이므로 순서 2, 3이 유지되어야 함
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

        // 다음 커서 및 페이지 존재 여부 검증
        assertNotNull(result.nextCursor, "데이터가 더 남아있으므로 nextCursor가 존재해야 한다.")
        assertEquals(3L, result.nextCursor, "nextCursor는 현재 페이지의 마지막 유저 ID인 3이어야 한다.")
    }

    @Test
    fun `마지막 페이지인 경우 nextCursor가 null이다`() = runTest {
        val targetUserId = UserId(1L)
        val pageSize = 3

        // pageSize + 1보다 적은 결과 -> 마지막 페이지
        val matchedUserIds = listOf(UserId(2L), UserId(3L))
        coEvery {
            discoverRepository.findUserIdsWithSimilarKeywords(
                targetUserId = targetUserId,
                cursor = null,
                pageSize = pageSize + 1,
            )
        } returns matchedUserIds

        coEvery {
            discoverRepository.fetchSharedUserKeywords(any())
        } returns matchedUserIds.map { createSharedUserKeyword(it.value, it.value * 10 + 1, 1L) }

        // when
        val result = usecase(targetUserId.value, null, pageSize)

        // then
        assertEquals(2, result.items.size)
        // 마지막 페이지이므로 nextCursor는 null이어야 한다.
        assertNull(result.nextCursor)
    }

    @Test
    fun `조회 결과가 없으면 빈 페이지를 반환한다`() = runTest {
        val targetUserId = UserId(1L)
        val pageSize = 10

        coEvery {
            discoverRepository.findUserIdsWithSimilarKeywords(
                targetUserId = targetUserId,
                cursor = null,
                pageSize = pageSize + 1,
            )
        } returns emptyList()

        // when
        val result = usecase(targetUserId.value, null, pageSize)

        // then
        assertTrue(result.items.isEmpty())
        assertNull(result.nextCursor)
        // fetchSharedUserKeywords는 호출되지 않아야 함
        coVerify(exactly = 0) { discoverRepository.fetchSharedUserKeywords(any()) }
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
    }
}
