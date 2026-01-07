package com.peekr.domain.discover.application.usecase

import com.peekr.common.model.KeywordName
import com.peekr.common.model.UserName
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.discover.domain.model.SharedUserKeyword
import com.peekr.domain.discover.domain.repository.DiscoverRepository
import com.peekr.util.TestDatabaseFactory
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import org.junit.After
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
        val keywordCount = 3 // 각 사용자가 등록한 키워드 개수
        val userCount = 3 // 사용자 개수
        val pageSize = 2

        val sharedUserKeywords = List(userCount) { userIdx ->
            val userId = userIdx + 1L
            List(keywordCount) { keywordIdx ->
                val keywordId = keywordIdx + 1L
                val userKeywordId = (userId * 10) + keywordId // 사용자 키워드 ID는 임의 생성
                createSharedUserKeyword(userId, userKeywordId, keywordId)
            }
        }.flatten()

        // findUserIdsWithSimilarKeywords 조회는 실제로 (pageSize + 1)개가 조회되기 때문에 3명이 조회되었다고 가정
        val matchedUserIds = listOf(UserId(1L), UserId(2L), UserId(3L))
        coEvery {
            discoverRepository.findUserIdsWithSimilarKeywords(
                targetUserId = targetUserId,
                cursor = null,
                pageSize = pageSize,
            )
        } returns matchedUserIds

        // fetchSharedUserKeywords는 1, 2번 유저의 키워드만 요청받음 (pageSize가 2기 때문에)
        val requestedIds = listOf(UserId(1L), UserId(2L))
        coEvery {
            discoverRepository.fetchSharedUserKeywords(any())
        } returns sharedUserKeywords.filter { it.userId in requestedIds }

        // when
        val result = usecase(targetUserId.value, null, pageSize)

        // then
        // 페이지네이션 크기 검증
        assertEquals(pageSize, result.items.size)

        // 데이터 순서 검증 (1, 2, 3) 순서이므로 순서 1, 2가 유지되어야 함
        assertEquals(
            1L,
            result.items[0]
                .user.id.value,
        )
        assertEquals(
            2L,
            result.items[1]
                .user.id.value,
        )

        // 키워드 그룹핑 검증
        assertEquals(keywordCount, result.items[0].keywords.size)
        assertEquals(keywordCount, result.items[1].keywords.size)

        // 다음 커서 및 페이지 존재 여부 검증
        assertNotNull(result.nextCursor, "데이터가 더 남아있으므로 nextCursor가 존재해야 한다.")
        assertEquals(2L, result.nextCursor, "nextCursor는 현재 페이지의 마지막 유저 ID인 2여야 한다.")
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
