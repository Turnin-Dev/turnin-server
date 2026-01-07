package com.peekr.domain.discover.infrastructure.repository

import com.peekr.common.db.schema.Keywords
import com.peekr.common.db.schema.UserKeywords
import com.peekr.common.db.schema.Users
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.id.UserId
import com.peekr.domain.discover.util.DiscoverTestDataGenerator.setupKeywordRelations
import com.peekr.domain.discover.util.TestVectorFixture
import com.peekr.domain.discover.util.TestVectorFixture.toPgVectorString
import com.peekr.util.TestDBContainerFactory
import com.peekr.util.TestDatabaseFactory
import junit.framework.TestCase.assertTrue
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.junit.After
import org.junit.Before
import org.junit.Test

class DiscoverRepositoryImplTest {
    private val repository = DiscoverRepositoryImpl()

    @Before
    fun setUp() {
        TestDBContainerFactory.init()
    }

    @After
    fun teardown() {
        TestDBContainerFactory.cleanUp()
    }

    @Test
    fun `findUserIdsWithSimilarKeywords - 유사도가 0_7 이상인 유저만 정확히 조회되어야 한다`() = runTest {
        // given: 데이터 세팅

        // 1번 키워드 벡터 값: 기준 (1, 0, 0 ...)
        val baseVector = TestVectorFixture.unitVector(1.0f)
        // 2번 키워드 벡터 값: 동일 (1, 0, 0 ...) -> 유사도 1.0 (성공)
        val sameVector = TestVectorFixture.unitVector(1.0f)
        // 3번 키워드 벡터 값: 완전 다름 (0, ..., 1) -> 유사도 0.0 (실패)
        val diffVector = TestVectorFixture.orthogonalVector()

        val targetUserId = UserId(1L)

        setupKeywordRelations(
            userCount = 3,
            keywordsWithVectors = listOf(
                // 1번 키워드
                "BaseKey" to baseVector,
                // 2번 키워드
                "SameKey" to sameVector,
                // 3번 키워드
                "DiffKey" to diffVector,
            ),
            userKeywordRelation = mapOf(
                // 나 (기준)
                targetUserId.value to listOf(1L),
                // 상대 1 (유사도 1.0 -> 조회 대상)
                2L to listOf(2L),
                // 상대 2 (유사도 0.0 -> 제외 대상)
                3L to listOf(3L),
            ),
        )

        // when
        val result = TestDatabaseFactory.dbQuery {
            repository.findUserIdsWithSimilarKeywords(
                targetUserId = targetUserId,
                cursor = null,
                pageSize = 10,
            )
        }

        // then
        assertEquals(1, result.size, "조회된 유저 수는 1명이어야 합니다.")
        assertEquals(2L, result.first().value, "유사도가 높은 2번 유저가 조회되어야 합니다.")
    }

    @Test
    fun `fetchSharedUserKeywords - 사용자 ID 리스트를 전달하면 해당 사용자들의 상세 정보와 키워드 목록을 최신순으로 반환한다`() = runTest {
        // given: 데이터 세팅
        val fakeVector = TestVectorFixture.unitVector(1.0f)
        val (user1, user2) = TestDatabaseFactory.dbQuery {
            // 사용자 생성
            val u1 = Users.insertAndGetId {
                it[name] = "테스트유저1"
                it[role] = Role.USER
                it[provider] = SocialLoginProvider.KAKAO
                it[providerId] = "p1"
                it[displayId] = "d1"
            }
            val u2 = Users.insertAndGetId {
                it[name] = "테스트유저2"
                it[role] = Role.USER
                it[provider] = SocialLoginProvider.KAKAO
                it[providerId] = "p2"
                it[displayId] = "d2"
            }

            // 키워드 생성
            val k1 = Keywords.insertAndGetId {
                it[keyword] = "키워드1"
                it[createdBy] = u1
                it[embedding] = fakeVector.toPgVectorString()
            }
            val k2 = Keywords.insertAndGetId {
                it[keyword] = "키워드2"
                it[createdBy] = u1
                it[embedding] = fakeVector.toPgVectorString()
            }

            // 관계 생성 (u1: [k1, k2], u2: [k2])
            UserKeywords.insert {
                it[userId] = u1
                it[keywordId] = k1
            }
            UserKeywords.insert {
                it[userId] = u1
                it[keywordId] = k2
            }
            UserKeywords.insert {
                it[userId] = u2
                it[keywordId] = k2
            }

            u1 to u2
        }

        val targetIds = listOf(UserId(user1.value), UserId(user2.value))

        // when
        val result = TestDatabaseFactory.dbQuery {
            repository.fetchSharedUserKeywords(targetIds)
        }

        // then
        // 유저 1은 키워드 2개, 유저 2는 1개이므로 총 3개의 행이 반환되어야 함
        assertEquals(3, result.size)

        // ORDER BY Users.id DESC에 따라 ID가 큰 user2가 먼저 나와야 함
        assertEquals(user2.value, result[0].userId.value)
        assertEquals("테스트유저2", result[0].userName.value)
        assertEquals("키워드2", result[0].keywordName.value)

        // 그다음 user1의 데이터들이 나옴
        assertEquals(user1.value, result[1].userId.value)
        assertEquals("테스트유저1", result[1].userName.value)

        // 유저 1이 가진 키워드들이 포함되어 있는지 확인
        val user1Keywords = result.filter { it.userId.value == user1.value }.map { it.keywordName.value }
        assertTrue(user1Keywords.containsAll(listOf("키워드1", "키워드2")))
    }
}
