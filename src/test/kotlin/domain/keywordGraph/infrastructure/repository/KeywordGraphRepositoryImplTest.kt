package com.peekr.domain.keywordGraph.infrastructure.repository

import com.peekr.common.model.id.UserId
import com.peekr.domain.keywordGraph.util.TestDataGenerator
import com.peekr.util.TestDatabaseFactory
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class KeywordGraphRepositoryImplTest {
    private val repository = KeywordGraphRepositoryImpl()

    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @After
    fun teardown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `getSharedKeywordInfos - 특정 사용자의 키워드를 같이 공유하고 있는 사용자 조회`() = runTest {
        // given
        val targetUser = UserId(1L)
        val expectedKeywordIds = listOf(1L, 2L, 3L)
        TestDataGenerator.generate(
            userCount = 26,
            keywordCount = 100,
            userKeywordRelation = mapOf(
                // 기준 사용자 키워드 등록
                targetUser.value to listOf(1L, 2L, 3L, 4L, 5L),
                // 기준 사용자와 공유된 키워드가 없는 사용자
                2L to listOf(10L),
                // 기준 사용자와 공유된 키워드가 3개 존재
                3L to expectedKeywordIds,
            ),
        )

        // when
        val result = repository.getSharedKeywordInfos(
            userId = targetUser,
            cursor = 10,
            pageSize = 10,
        )
        val actualKeywordIds = result
            .find { it.userId.value == 3L }
            ?.keywordIds
            ?.map { it.value }

        // then
        assertTrue(result.isNotEmpty())
        assertEquals(expectedKeywordIds, actualKeywordIds)
    }
}
