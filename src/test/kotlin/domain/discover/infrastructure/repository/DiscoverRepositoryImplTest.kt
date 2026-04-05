package com.peekr.domain.discover.infrastructure.repository

import com.peekr.common.db.schema.BlockReasons
import com.peekr.common.db.schema.Blocks
import com.peekr.common.db.schema.Keywords
import com.peekr.common.db.schema.UserKeywords
import com.peekr.common.db.schema.Users
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.discover.util.DiscoverTestDataGenerator.setupKeywordRelations
import com.peekr.domain.discover.util.TestVectorFixture
import com.peekr.domain.discover.util.TestVectorFixture.toPgVectorString
import com.peekr.util.db.PostgresRule
import com.peekr.util.db.setUserKeywordInactiveForTest
import java.time.Instant
import junit.framework.TestCase.assertTrue
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.junit.Rule
import org.junit.Test

class DiscoverRepositoryImplTest {
    @get:Rule
    val dbRule = PostgresRule()

    private val repository = DiscoverRepositoryImpl()

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
        val result = repository.findUserIdsWithSimilarKeywords(
            targetUserId = targetUserId,
            cursor = null,
            pageSize = 10,
        )

        // then
        assertEquals(1, result.size, "조회된 유저 수는 1명이어야 합니다.")
        assertEquals(2L, result.first().value, "유사도가 높은 2번 유저가 조회되어야 합니다.")
    }

    @Test
    fun `findUserIdsWithSimilarKeywords - 자기 자신은 결과에서 제외되어야 한다`() = runTest {
        // given
        val baseVector = TestVectorFixture.unitVector(1.0f)
        val targetUserId = UserId(1L)

        // 본인에게만 키워드를 할당 (유사 키워드를 가진 다른 유저 없음)
        setupKeywordRelations(
            userCount = 1,
            keywordsWithVectors = listOf("BaseKey" to baseVector),
            userKeywordRelation = mapOf(
                targetUserId.value to listOf(1L),
            ),
        )

        // when
        val result = repository.findUserIdsWithSimilarKeywords(
            targetUserId = targetUserId,
            cursor = null,
            pageSize = 10,
        )

        // then: 자기 자신은 포함되지 않아야 한다
        assertEquals(0, result.size)
        assertTrue(result.none { it == targetUserId })
    }

    @Test
    fun `findUserIdsWithSimilarKeywords - 비활성화 사용자 키워드는 조회되지 않는다`() = runTest {
        // given: 데이터 세팅, 비활성화 사용자 키워드 생성

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

        // 조회 대상인 사용자 키워드 ID가 2인 사용자 키워드 비활성화 설정
        setUserKeywordInactiveForTest(UserKeywordId(2L))

        // when
        val result = repository.findUserIdsWithSimilarKeywords(
            targetUserId = targetUserId,
            cursor = null,
            pageSize = 10,
        )

        // then: 조회되지 않아야 한다.
        assertEquals(0, result.size)
    }

    @Test
    fun `findUserIdsWithSimilarKeywords - 차단된 사용자는 조회되지 않는다`() = runTest {
        // given: 데이터 세팅
        val baseVector = TestVectorFixture.unitVector(1.0f)
        val sameVector = TestVectorFixture.unitVector(1.0f)

        val targetUserId = UserId(1L)
        val blockedUserId = UserId(2L)

        setupKeywordRelations(
            userCount = 2,
            keywordsWithVectors = listOf(
                "BaseKey" to baseVector,
                "SameKey" to sameVector,
            ),
            userKeywordRelation = mapOf(
                targetUserId.value to listOf(1L),
                blockedUserId.value to listOf(2L),
            ),
        )

        // 차단 수행
        setUpBlock(
            blockerId = targetUserId,
            blockedId = blockedUserId,
        )

        // when
        val result = repository.findUserIdsWithSimilarKeywords(
            targetUserId = targetUserId,
            cursor = null,
            pageSize = 10,
        )

        // then
        assertEquals(0, result.size)
    }

    @Test
    fun `findUserIdsWithSimilarKeywords - 차단 당한 사용자는 차단한 사용자가 조회되지 않는다`() = runTest {
        // given: 데이터 세팅
        val baseVector = TestVectorFixture.unitVector(1.0f)
        val sameVector = TestVectorFixture.unitVector(1.0f)

        val targetUserId = UserId(1L)
        val blockedUserId = UserId(2L)

        setupKeywordRelations(
            userCount = 2,
            keywordsWithVectors = listOf(
                "BaseKey" to baseVector,
                "SameKey" to sameVector,
            ),
            userKeywordRelation = mapOf(
                targetUserId.value to listOf(1L),
                blockedUserId.value to listOf(2L),
            ),
        )

        // targetUser가 blockedUser를 차단
        setUpBlock(
            blockerId = targetUserId,
            blockedId = blockedUserId,
        )

        // when: 차단 당한 사용자(blockedUserId) 입장에서 조회
        val result = repository.findUserIdsWithSimilarKeywords(
            targetUserId = blockedUserId,
            cursor = null,
            pageSize = 10,
        )

        // then: 자신을 차단한 사용자는 조회되지 않아야 한다
        assertEquals(0, result.size)
    }

    @Test
    fun `findUserIdsWithSimilarKeywords - pageSize만큼만 조회된다`() = runTest {
        // given: 유사도 높은 유저 5명 세팅
        val baseVector = TestVectorFixture.unitVector(1.0f)
        val targetUserId = UserId(1L)

        // userCount - targetUser 포함 6명
        setupKeywordRelations(
            userCount = 6,
            keywordsWithVectors = listOf(
                "BaseKey" to baseVector,
                "Key2" to TestVectorFixture.unitVector(1.0f),
                "Key3" to TestVectorFixture.unitVector(1.0f),
                "Key4" to TestVectorFixture.unitVector(1.0f),
                "Key5" to TestVectorFixture.unitVector(1.0f),
                "Key6" to TestVectorFixture.unitVector(1.0f),
            ),
            userKeywordRelation = mapOf(
                targetUserId.value to listOf(1L),
                2L to listOf(2L),
                3L to listOf(3L),
                4L to listOf(4L),
                5L to listOf(5L),
                6L to listOf(6L),
            ),
        )

        // when: pageSize = 3으로 제한
        val result = repository.findUserIdsWithSimilarKeywords(
            targetUserId = targetUserId,
            cursor = null,
            pageSize = 3,
        )

        // then: pageSize인 3건만 반환되어야 한다 (pageSize + 1 이 아님)
        assertEquals(3, result.size)
    }

    @Test
    fun `findUserIdsWithSimilarKeywords - cursor가 주어지면 해당 user_id보다 작은 유저만 조회된다`() = runTest {
        // given: 유사도 높은 유저 4명 세팅 (user_id: 2, 3, 4, 5)
        val baseVector = TestVectorFixture.unitVector(1.0f)
        val targetUserId = UserId(1L)

        setupKeywordRelations(
            userCount = 5,
            keywordsWithVectors = listOf(
                "BaseKey" to baseVector,
                "Key2" to TestVectorFixture.unitVector(1.0f),
                "Key3" to TestVectorFixture.unitVector(1.0f),
                "Key4" to TestVectorFixture.unitVector(1.0f),
                "Key5" to TestVectorFixture.unitVector(1.0f),
            ),
            userKeywordRelation = mapOf(
                targetUserId.value to listOf(1L),
                2L to listOf(2L),
                3L to listOf(3L),
                4L to listOf(4L),
                5L to listOf(5L),
            ),
        )

        // when: cursor = 4 (user_id < 4인 유저만 조회)
        val result = repository.findUserIdsWithSimilarKeywords(
            targetUserId = targetUserId,
            cursor = 4L,
            pageSize = 10,
        )

        // then: user_id가 4 미만인 2, 3번 유저만 반환되어야 한다 (ORDER BY user_id DESC)
        assertEquals(2, result.size)
        assertEquals(listOf(3L, 2L), result.map { it.value })
    }

    @Test
    fun `findUserIdsWithSimilarKeywords - cursor와 pageSize를 함께 사용하면 커서 이후 pageSize만큼만 조회된다`() = runTest {
        // given: 유사도 높은 유저 5명 세팅 (user_id: 2, 3, 4, 5, 6)
        val baseVector = TestVectorFixture.unitVector(1.0f)
        val targetUserId = UserId(1L)

        setupKeywordRelations(
            userCount = 6,
            keywordsWithVectors = listOf(
                "BaseKey" to baseVector,
                "Key2" to TestVectorFixture.unitVector(1.0f),
                "Key3" to TestVectorFixture.unitVector(1.0f),
                "Key4" to TestVectorFixture.unitVector(1.0f),
                "Key5" to TestVectorFixture.unitVector(1.0f),
                "Key6" to TestVectorFixture.unitVector(1.0f),
            ),
            userKeywordRelation = mapOf(
                targetUserId.value to listOf(1L),
                2L to listOf(2L),
                3L to listOf(3L),
                4L to listOf(4L),
                5L to listOf(5L),
                6L to listOf(6L),
            ),
        )

        // when: cursor = 6, pageSize = 2 -> user_id < 6 중 상위 2건 (5, 4)
        val result = repository.findUserIdsWithSimilarKeywords(
            targetUserId = targetUserId,
            cursor = 6L,
            pageSize = 2,
        )

        // then
        assertEquals(2, result.size)
        assertEquals(listOf(5L, 4L), result.map { it.value })
    }

    @Test
    fun `fetchSharedUserKeywords - 사용자 ID 리스트를 전달하면 해당 사용자들의 상세 정보와 키워드 목록을 최신순으로 반환한다`() = runTest {
        // given: 데이터 세팅
        val fakeVector = TestVectorFixture.unitVector(1.0f)
        val (user1, user2) = dbRule.dbQuery {
            // 사용자 생성
            val u1 = Users.insertAndGetId {
                it[name] = "테스트유저1"
                it[role] = Role.USER
                it[provider] = SocialLoginProvider.KAKAO
                it[providerId] = "p1"
                it[displayId] = "d1"
                it[introduce] = ""
                it[lastLoginAt] = Instant.now()
            }
            val u2 = Users.insertAndGetId {
                it[name] = "테스트유저2"
                it[role] = Role.USER
                it[provider] = SocialLoginProvider.KAKAO
                it[providerId] = "p2"
                it[displayId] = "d2"
                it[introduce] = ""
                it[lastLoginAt] = Instant.now()
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
        val result = repository.fetchSharedUserKeywords(targetIds)

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

    @Test
    fun `fetchSharedUserKeywords - 빈 ID 리스트를 전달하면 빈 결과를 반환한다`() = runTest {
        // when
        val result = repository.fetchSharedUserKeywords(emptyList())

        // then
        assertTrue(result.isEmpty())
    }

    private suspend fun setUpBlock(
        blockerId: UserId,
        blockedId: UserId,
    ) = dbRule.dbQuery {
        Blocks.insert {
            it[this.blockerId] = EntityID(blockerId.value, Users)
            it[this.blockedId] = EntityID(blockedId.value, Users)
            it[this.reasonId] = EntityID(1L, BlockReasons)
            it[this.customReason] = "custom reason"
        }
    }
}
