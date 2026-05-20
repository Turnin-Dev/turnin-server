package com.turnin.domain.discover.infrastructure.repository

import com.turnin.common.db.schema.BlockReasons
import com.turnin.common.db.schema.Blocks
import com.turnin.common.db.schema.Keywords
import com.turnin.common.db.schema.UserKeywords
import com.turnin.common.db.schema.Users
import com.turnin.common.ml.keywordCategory.KeywordCategory
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.discover.util.DiscoverTestDataGenerator.setupKeywordRelations
import com.turnin.domain.discover.util.TestVectorFixture
import com.turnin.domain.discover.util.TestVectorFixture.toPgVectorString
import com.turnin.util.db.PostgresRule
import com.turnin.util.db.setUserKeywordInactiveForTest
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
    fun `findUserIdsWithSimilarKeywords - 같은 카테고리 키워드를 가진 유저만 조회되어야 한다`() = runTest {
        // given
        val targetUserId = UserId(1L)

        setupKeywordRelations(
            userCount = 3,
            keywordsWithCategories = listOf(
                // 1번: 나의 키워드
                "BaseKey" to KeywordCategory.FOOD,
                // 2번: 같은 카테고리 → 조회 대상
                "SameKey" to KeywordCategory.FOOD,
                // 3번: 다른 카테고리 → 제외
                "DiffKey" to KeywordCategory.TECH,
            ),
            userKeywordRelation = mapOf(
                targetUserId.value to listOf(1L),
                2L to listOf(2L),
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
        assertEquals(1, result.size)
        assertEquals(2L, result.first().value)
    }

    @Test
    fun `findUserIdsWithSimilarKeywords - 카테고리가 없는 키워드는 조회되지 않는다`() = runTest {
        // given
        val targetUserId = UserId(1L)

        setupKeywordRelations(
            userCount = 2,
            keywordsWithCategories = listOf(
                // 1번: 나의 키워드
                "BaseKey" to KeywordCategory.FOOD,
                // 2번: 미분류 → 제외
                "NullKey" to null,
            ),
            userKeywordRelation = mapOf(
                targetUserId.value to listOf(1L),
                2L to listOf(2L),
            ),
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
    fun `findUserIdsWithSimilarKeywords - 자기 자신은 결과에서 제외되어야 한다`() = runTest {
        // given
        val targetUserId = UserId(1L)

        setupKeywordRelations(
            userCount = 1,
            keywordsWithCategories = listOf("BaseKey" to KeywordCategory.FOOD),
            userKeywordRelation = mapOf(targetUserId.value to listOf(1L)),
        )

        // when
        val result = repository.findUserIdsWithSimilarKeywords(
            targetUserId = targetUserId,
            cursor = null,
            pageSize = 10,
        )

        // then
        assertEquals(0, result.size)
        assertTrue(result.none { it == targetUserId })
    }

    @Test
    fun `findUserIdsWithSimilarKeywords - 비활성화 사용자 키워드는 조회되지 않는다`() = runTest {
        // given
        val targetUserId = UserId(1L)

        setupKeywordRelations(
            userCount = 2,
            keywordsWithCategories = listOf(
                "BaseKey" to KeywordCategory.FOOD,
                "SameKey" to KeywordCategory.FOOD,
            ),
            userKeywordRelation = mapOf(
                targetUserId.value to listOf(1L),
                2L to listOf(2L),
            ),
        )

        setUserKeywordInactiveForTest(UserKeywordId(2L))

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
    fun `findUserIdsWithSimilarKeywords - 차단된 사용자는 조회되지 않는다`() = runTest {
        // given
        val targetUserId = UserId(1L)
        val blockedUserId = UserId(2L)

        setupKeywordRelations(
            userCount = 2,
            keywordsWithCategories = listOf(
                "BaseKey" to KeywordCategory.FOOD,
                "SameKey" to KeywordCategory.FOOD,
            ),
            userKeywordRelation = mapOf(
                targetUserId.value to listOf(1L),
                blockedUserId.value to listOf(2L),
            ),
        )

        setUpBlock(blockerId = targetUserId, blockedId = blockedUserId)

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
        // given
        val targetUserId = UserId(1L)
        val blockedUserId = UserId(2L)

        setupKeywordRelations(
            userCount = 2,
            keywordsWithCategories = listOf(
                "BaseKey" to KeywordCategory.FOOD,
                "SameKey" to KeywordCategory.FOOD,
            ),
            userKeywordRelation = mapOf(
                targetUserId.value to listOf(1L),
                blockedUserId.value to listOf(2L),
            ),
        )

        setUpBlock(blockerId = targetUserId, blockedId = blockedUserId)

        // when: 차단 당한 사용자 입장에서 조회
        val result = repository.findUserIdsWithSimilarKeywords(
            targetUserId = blockedUserId,
            cursor = null,
            pageSize = 10,
        )

        // then
        assertEquals(0, result.size)
    }

    @Test
    fun `findUserIdsWithSimilarKeywords - pageSize만큼만 조회된다`() = runTest {
        // given
        val targetUserId = UserId(1L)

        setupKeywordRelations(
            userCount = 6,
            keywordsWithCategories = listOf(
                "BaseKey" to KeywordCategory.FOOD,
                "Key2" to KeywordCategory.FOOD,
                "Key3" to KeywordCategory.FOOD,
                "Key4" to KeywordCategory.FOOD,
                "Key5" to KeywordCategory.FOOD,
                "Key6" to KeywordCategory.FOOD,
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

        // when
        val result = repository.findUserIdsWithSimilarKeywords(
            targetUserId = targetUserId,
            cursor = null,
            pageSize = 3,
        )

        // then
        assertEquals(3, result.size)
    }

    @Test
    fun `findUserIdsWithSimilarKeywords - cursor가 주어지면 해당 user_id보다 작은 유저만 조회된다`() = runTest {
        // given
        val targetUserId = UserId(1L)

        setupKeywordRelations(
            userCount = 5,
            keywordsWithCategories = listOf(
                "BaseKey" to KeywordCategory.FOOD,
                "Key2" to KeywordCategory.FOOD,
                "Key3" to KeywordCategory.FOOD,
                "Key4" to KeywordCategory.FOOD,
                "Key5" to KeywordCategory.FOOD,
            ),
            userKeywordRelation = mapOf(
                targetUserId.value to listOf(1L),
                2L to listOf(2L),
                3L to listOf(3L),
                4L to listOf(4L),
                5L to listOf(5L),
            ),
        )

        // when
        val result = repository.findUserIdsWithSimilarKeywords(
            targetUserId = targetUserId,
            cursor = 4L,
            pageSize = 10,
        )

        // then
        assertEquals(2, result.size)
        assertEquals(listOf(3L, 2L), result.map { it.value })
    }

    @Test
    fun `findUserIdsWithSimilarKeywords - cursor와 pageSize를 함께 사용하면 커서 이후 pageSize만큼만 조회된다`() = runTest {
        // given
        val targetUserId = UserId(1L)

        setupKeywordRelations(
            userCount = 6,
            keywordsWithCategories = listOf(
                "BaseKey" to KeywordCategory.FOOD,
                "Key2" to KeywordCategory.FOOD,
                "Key3" to KeywordCategory.FOOD,
                "Key4" to KeywordCategory.FOOD,
                "Key5" to KeywordCategory.FOOD,
                "Key6" to KeywordCategory.FOOD,
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

        // when
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
