package com.turnin.domain.discover.infrastructure.repository

import com.turnin.common.db.schema.BlockReasons
import com.turnin.common.db.schema.Blocks
import com.turnin.common.db.schema.Keywords
import com.turnin.common.db.schema.UserKeywords
import com.turnin.common.db.schema.Users
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.discover.domain.model.DiscoverCursor
import com.turnin.domain.discover.domain.repository.DiscoveredResult
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

    /** 테스트 편의를 위한 기본값 래퍼. seed/threshold를 매번 안 적어도 되게 함 */
    private suspend fun search(
        targetUserId: UserId,
        viewerUserId: UserId? = null,
        seed: String = DEFAULT_SEED,
        similarityThreshold: Double = DEFAULT_THRESHOLD,
        cursor: DiscoverCursor? = null,
        pageSize: Int = 10,
    ): List<DiscoveredResult> = repository.findUserIdsWithSimilarKeywords(
        targetUserId = targetUserId,
        viewerUserId = viewerUserId,
        seed = seed,
        similarityThreshold = similarityThreshold,
        cursor = cursor,
        pageSize = pageSize,
    )

    @Test
    fun `findUserIdsWithSimilarKeywords - 자기 자신은 결과에서 제외되어야 한다`() = runTest {
        // given
        val targetUserId = UserId(1L)
        val baseVector = TestVectorFixture.unitVector(1.0f)

        setupKeywordRelations(
            userCount = 1,
            keywordsWithVectors = listOf("BaseKey" to baseVector),
            userKeywordRelation = mapOf(targetUserId.value to listOf(1L)),
        )

        // when
        val result = search(targetUserId = targetUserId)

        // then
        assertEquals(0, result.size)
        assertTrue(result.none { it.userId == targetUserId })
    }

    @Test
    fun `findUserIdsWithSimilarKeywords - viewerUserId가 주어지면 해당 유저도 결과에서 제외되어야 한다`() = runTest {
        // given
        val targetUserId = UserId(1L)
        val viewerUserId = UserId(2L)
        val testVector = TestVectorFixture.unitVector(1.0f)

        setupKeywordRelations(
            userCount = 3,
            keywordsWithVectors = listOf(
                // 1번 키워드
                "BaseKey" to testVector,
                // 2번 키워드
                "SameKey" to testVector,
                // 3번 키워드
                "OtherKey" to testVector,
            ),
            userKeywordRelation = mapOf(
                targetUserId.value to listOf(1L),
                viewerUserId.value to listOf(2L),
                3L to listOf(3L),
            ),
        )

        // when
        val result = search(targetUserId = targetUserId, viewerUserId = viewerUserId)

        // then: viewer(2L)는 제외되고 3L만 조회되어야 함
        assertEquals(1, result.size)
        assertEquals(3L, result.first().userId.value)
    }

    @Test
    fun `findUserIdsWithSimilarKeywords - viewerUserId가 null이면 필터링되지 않는다`() = runTest {
        // given
        val targetUserId = UserId(1L)
        val testVector = TestVectorFixture.unitVector(1.0f)

        setupKeywordRelations(
            userCount = 2,
            keywordsWithVectors = listOf(
                "BaseKey" to testVector,
                "SameKey" to testVector,
            ),
            userKeywordRelation = mapOf(
                targetUserId.value to listOf(1L),
                2L to listOf(2L),
            ),
        )

        // when
        val result = search(targetUserId = targetUserId, viewerUserId = null)

        // then
        assertEquals(1, result.size)
        assertEquals(2L, result.first().userId.value)
    }

    @Test
    fun `findUserIdsWithSimilarKeywords - 비활성화 사용자 키워드는 조회되지 않는다`() = runTest {
        // given
        val targetUserId = UserId(1L)
        val testVector = TestVectorFixture.unitVector(1.0f)

        setupKeywordRelations(
            userCount = 2,
            keywordsWithVectors = listOf(
                "BaseKey" to testVector,
                "SameKey" to testVector,
            ),
            userKeywordRelation = mapOf(
                targetUserId.value to listOf(1L),
                2L to listOf(2L),
            ),
        )

        setUserKeywordInactiveForTest(UserKeywordId(2L))

        // when
        val result = search(targetUserId = targetUserId)

        // then
        assertEquals(0, result.size)
    }

    @Test
    fun `findUserIdsWithSimilarKeywords - 차단된 사용자는 조회되지 않는다`() = runTest {
        // given
        val targetUserId = UserId(1L)
        val blockedUserId = UserId(2L)
        val testVector = TestVectorFixture.unitVector(1.0f)

        setupKeywordRelations(
            userCount = 2,
            keywordsWithVectors = listOf(
                "BaseKey" to testVector,
                "SameKey" to testVector,
            ),
            userKeywordRelation = mapOf(
                targetUserId.value to listOf(1L),
                blockedUserId.value to listOf(2L),
            ),
        )

        setUpBlock(blockerId = targetUserId, blockedId = blockedUserId)

        // when
        val result = search(targetUserId = targetUserId)

        // then
        assertEquals(0, result.size)
    }

    @Test
    fun `findUserIdsWithSimilarKeywords - 차단 당한 사용자는 차단한 사용자가 조회되지 않는다`() = runTest {
        // given
        val targetUserId = UserId(1L)
        val blockedUserId = UserId(2L)
        val testVector = TestVectorFixture.unitVector(1.0f)

        setupKeywordRelations(
            userCount = 2,
            keywordsWithVectors = listOf(
                "BaseKey" to testVector,
                "SameKey" to testVector,
            ),
            userKeywordRelation = mapOf(
                targetUserId.value to listOf(1L),
                blockedUserId.value to listOf(2L),
            ),
        )

        setUpBlock(blockerId = targetUserId, blockedId = blockedUserId)

        // when: 차단 당한 사용자 입장에서 조회
        val result = search(targetUserId = blockedUserId)

        // then
        assertEquals(0, result.size)
    }

    @Test
    fun `findUserIdsWithSimilarKeywords - pageSize만큼만 조회된다`() = runTest {
        // given
        val targetUserId = UserId(1L)
        val testVector = TestVectorFixture.unitVector(1.0f)

        setupKeywordRelations(
            userCount = 6,
            keywordsWithVectors = listOf(
                "BaseKey" to testVector,
                "Key2" to testVector,
                "Key3" to testVector,
                "Key4" to testVector,
                "Key5" to testVector,
                "Key6" to testVector,
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
        val result = search(targetUserId = targetUserId, pageSize = 3)

        // then: match_score/shuffle_key가 seed 의존적이라 어떤 유저가 뽑히는지는 검증하지 않고 개수만 확인
        assertEquals(3, result.size)
        assertEquals(3, result.map { it.userId }.distinct().size) // 중복 없음
    }

    @Test
    fun `findUserIdsWithSimilarKeywords - cursor가 주어지면 해당 지점 이후의 결과만 조회된다`() = runTest {
        // given
        val targetUserId = UserId(1L)
        val testVector = TestVectorFixture.unitVector(1.0f)

        setupKeywordRelations(
            userCount = 5,
            keywordsWithVectors = listOf(
                "BaseKey" to testVector,
                "Key2" to testVector,
                "Key3" to testVector,
                "Key4" to testVector,
                "Key5" to testVector,
            ),
            userKeywordRelation = mapOf(
                targetUserId.value to listOf(1L),
                2L to listOf(2L),
                3L to listOf(3L),
                4L to listOf(4L),
                5L to listOf(5L),
            ),
        )

        // 커서 없이 전체 조회해서 이 seed/데이터 기준의 "정답 순서"를 확보
        val baseline = search(targetUserId = targetUserId, pageSize = 10)
        assertEquals(4, baseline.size) // 대상 유저 4명 (2~5L)

        // 2번째 결과를 커서로 사용
        val cursorPoint = baseline[1]
        val cursor = DiscoverCursor(
            lastScore = cursorPoint.matchScore,
            lastShuffleKey = cursorPoint.shuffleKey,
            lastUserId = cursorPoint.userId.value,
        )

        // when
        val result = search(targetUserId = targetUserId, cursor = cursor, pageSize = 10)

        // then: baseline의 커서 이후 항목들과 정확히 일치해야 함
        assertEquals(baseline.drop(2).map { it.userId }, result.map { it.userId })
    }

    @Test
    fun `findUserIdsWithSimilarKeywords - cursor와 pageSize를 함께 사용하면 커서 이후 pageSize만큼만 조회된다`() = runTest {
        // given
        val targetUserId = UserId(1L)
        val testVector = TestVectorFixture.unitVector(1.0f)

        setupKeywordRelations(
            userCount = 6,
            keywordsWithVectors = listOf(
                "BaseKey" to testVector,
                "Key2" to testVector,
                "Key3" to testVector,
                "Key4" to testVector,
                "Key5" to testVector,
                "Key6" to testVector,
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

        val baseline = search(targetUserId = targetUserId, pageSize = 10)
        assertEquals(5, baseline.size) // 대상 유저 5명 (2~6L)

        val cursorPoint = baseline[0]
        val cursor = DiscoverCursor(
            lastScore = cursorPoint.matchScore,
            lastShuffleKey = cursorPoint.shuffleKey,
            lastUserId = cursorPoint.userId.value,
        )

        // when
        val result = search(targetUserId = targetUserId, cursor = cursor, pageSize = 2)

        // then
        assertEquals(baseline.drop(1).take(2).map { it.userId }, result.map { it.userId })
    }

    @Test
    fun `findUserIdsWithSimilarKeywords - seed가 다르면 동점자 정렬 순서가 달라질 수 있다`() = runTest {
        // given
        val targetUserId = UserId(1L)
        val testVector = TestVectorFixture.unitVector(1.0f)

        setupKeywordRelations(
            userCount = 4,
            keywordsWithVectors = listOf(
                "BaseKey" to testVector,
                "Key2" to testVector,
                "Key3" to testVector,
                "Key4" to testVector,
            ),
            userKeywordRelation = mapOf(
                targetUserId.value to listOf(1L),
                2L to listOf(2L),
                3L to listOf(3L),
                4L to listOf(4L),
            ),
        )

        // when
        val resultA = search(targetUserId = targetUserId, seed = "seed-a")
        val resultB = search(targetUserId = targetUserId, seed = "seed-b")

        // then: 대상 유저 집합은 동일해야 함 (셔플만 다름)
        assertEquals(resultA.map { it.userId }.toSet(), resultB.map { it.userId }.toSet())

        // 같은 seed로 다시 조회하면 항상 동일한 순서가 나와야 함 (결정론적 셔플)
        val resultARepeat = search(targetUserId = targetUserId, seed = "seed-a")
        assertEquals(resultA.map { it.userId }, resultARepeat.map { it.userId })
    }

    @Test
    fun `fetchSharedUserKeywords - 사용자 ID 리스트를 전달하면 해당 사용자들의 상세 정보와 키워드 목록을 최신순으로 반환한다`() = runTest {
        // 변경 없음
        val fakeVector = TestVectorFixture.unitVector(1.0f)
        val (user1, user2) = dbRule.dbQuery {
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

        val result = repository.fetchSharedUserKeywords(targetIds)

        assertEquals(3, result.size)
        assertEquals(user2.value, result[0].userId.value)
        assertEquals("테스트유저2", result[0].userName.value)
        assertEquals("키워드2", result[0].keywordName.value)
        assertEquals(user1.value, result[1].userId.value)
        assertEquals("테스트유저1", result[1].userName.value)

        val user1Keywords = result.filter { it.userId.value == user1.value }.map { it.keywordName.value }
        assertTrue(user1Keywords.containsAll(listOf("키워드1", "키워드2")))
    }

    @Test
    fun `fetchSharedUserKeywords - 빈 ID 리스트를 전달하면 빈 결과를 반환한다`() = runTest {
        val result = repository.fetchSharedUserKeywords(emptyList())
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

    companion object {
        private const val DEFAULT_SEED = "test-seed"
        private const val DEFAULT_THRESHOLD = 0.5
    }
}
