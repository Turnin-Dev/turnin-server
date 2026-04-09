package com.peekr.domain.feed.infrastructure.repository

import com.peekr.common.db.schema.BlockEntity
import com.peekr.common.db.schema.BlockReasons
import com.peekr.common.db.schema.FriendEntity
import com.peekr.common.db.schema.KeywordEntity
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.db.schema.UserKeywordEntity
import com.peekr.common.db.schema.UserKeywords
import com.peekr.common.db.schema.Users
import com.peekr.common.model.FriendRequestStatus
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.common.util.toOffsetDateTime
import com.peekr.domain.discover.util.TestVectorFixture
import com.peekr.domain.discover.util.TestVectorFixture.toPgVectorString
import com.peekr.util.db.PostgresRule
import com.peekr.util.db.TestDatabaseFactory
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.update
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class FeedRepositoryImplTest {
    @get:Rule
    val dbRule = PostgresRule()

    private val repository = FeedRepositoryImpl()

    // ==========================================================================================
    // getFeeds 테스트
    // ==========================================================================================

    @Test
    fun `첫 페이지를 정상적으로 조회한다`() = runTest {
        // given
        val me = createUser("me")
        val friend = createUser("friend")
        val stranger = createUser("stranger")
        val blocked = createUser("blocked")

        val baseVector = TestVectorFixture.unitVector(1.0f)
        val similarVector = TestVectorFixture.unitVector(0.9f) // 유사도 0.7 이상 -> similar_pool 포함

        val myKeyword = createKeyword("myKeyword", baseVector.toPgVectorString(), me)
        val similarKeyword = createKeyword("similarKeyword", similarVector.toPgVectorString(), me)

        createUserKeyword(me, myKeyword, "내 취향 글")
        createUserKeyword(friend, similarKeyword, "친구의 유사한 글") // friend_pool: 점수 최상위
        createUserKeyword(stranger, similarKeyword, "남의 유사한 글") // similar_pool: 점수 중간
        createUserKeyword(blocked, similarKeyword, "차단된 사용자의 글") // 차단 -> 조회 X

        createFriends(requesterId = me.id, receiverId = friend.id, status = FriendRequestStatus.ACCEPTED)
        createBlocks(blockerId = me.id, blockedId = blocked.id, reasonId = EntityID(1L, BlockReasons))

        // when
        val myUserId = UserId(me.id.value)
        val feeds = repository.getFeeds(
            userId = myUserId,
            cursorScore = null,
            cursorCreatedAt = null,
            cursorUkId = null,
            limit = 10,
        )

        // then
        println("==========================================================================================")
        println("조회된 피드 개수: ${feeds.size}")
        feeds.forEach {
            println("작성자: ${it.userName}, 점수: ${it.score}, 유사도: ${it.similarity}, 키워드: ${it.keyword}")
        }
        println("==========================================================================================")

        // 필터링 검증: 내 글, 차단된 사용자 글 제외
        assertTrue(myUserId !in feeds.map { it.userId })
        assertTrue(UserId(blocked.id.value) !in feeds.map { it.userId })

        // 순서 검증: 1등(친구, similar) -> 2등(남, similar)
        assertEquals(2, feeds.size)
        val firstFeed = feeds[0]
        val secondFeed = feeds[1]

        // 1등: 친구 보너스(100) + 유사도 점수
        assertEquals(friend.id.value, firstFeed.userId.value)
        assertTrue(firstFeed.score >= 100.0)

        // 2등: 유사도 점수만
        assertEquals(stranger.id.value, secondFeed.userId.value)
        assertTrue(secondFeed.score < 100.0)
        assertTrue(firstFeed.score > secondFeed.score)
    }

    @Test
    fun `friend_pool이 similar_pool보다 우선순위가 높다`() = runTest {
        // given: 친구가 낮은 유사도, 타인이 높은 유사도를 가지는 경우
        val me = createUser("me")
        val friend = createUser("friend")
        val stranger = createUser("stranger")

        val baseVector = TestVectorFixture.unitVector(1.0f)
        val highSimilarVector = TestVectorFixture.unitVector(0.99f) // 타인: 유사도 매우 높음
        val lowSimilarVector = TestVectorFixture.unitVector(0.8f) // 친구: 유사도 낮음

        val myKeyword = createKeyword("myKeyword", baseVector.toPgVectorString(), me)
        val highKeyword = createKeyword("highKeyword", highSimilarVector.toPgVectorString(), me)
        val lowKeyword = createKeyword("lowKeyword", lowSimilarVector.toPgVectorString(), me)

        createUserKeyword(me, myKeyword, "내 글")
        createUserKeyword(friend, lowKeyword, "친구 글 (유사도 낮음)")
        createUserKeyword(stranger, highKeyword, "타인 글 (유사도 높음)")

        createFriends(requesterId = me.id, receiverId = friend.id, status = FriendRequestStatus.ACCEPTED)

        // when
        val feeds = repository.getFeeds(
            userId = UserId(me.id.value),
            cursorScore = null,
            cursorCreatedAt = null,
            cursorUkId = null,
            limit = 10,
        )

        // then: 친구 글이 더 낮은 유사도임에도 친구 보너스로 1등
        assertEquals(2, feeds.size)
        assertEquals(friend.id.value, feeds[0].userId.value)
        assertEquals(stranger.id.value, feeds[1].userId.value)
        assertTrue(feeds[0].score > feeds[1].score)
    }

    @Test
    fun `유사도 임계값 미만인 글은 similar_pool에서 제외된다`() = runTest {
        // given
        val me = createUser("me")
        val stranger1 = createUser("stranger1")
        val stranger2 = createUser("stranger2")

        val baseVector = TestVectorFixture.unitVector(1.0f)
        val aboveThresholdVector = TestVectorFixture.unitVector(0.8f) // 임계값(0.7) 이상
        val belowThresholdVector = TestVectorFixture.orthogonalVector() // 임계값(0.7) 미만

        val myKeyword = createKeyword("myKeyword", baseVector.toPgVectorString(), me)
        val aboveKeyword = createKeyword("aboveKeyword", aboveThresholdVector.toPgVectorString(), me)
        val belowKeyword = createKeyword("belowKeyword", belowThresholdVector.toPgVectorString(), me)

        createUserKeyword(me, myKeyword, "내 글")
        createUserKeyword(stranger1, aboveKeyword, "임계값 이상 글") // similar_pool 포함
        createUserKeyword(stranger2, belowKeyword, "임계값 미만 글") // similar_pool 제외, fallback_pool로

        // when
        val feeds = repository.getFeeds(
            userId = UserId(me.id.value),
            cursorScore = null,
            cursorCreatedAt = null,
            cursorUkId = null,
            limit = 10,
        )

        // then: 임계값 미만 글은 fallback_pool(priority=3, score=0)으로 처리됨
        val aboveFeed = feeds.find { it.userId == UserId(stranger1.id.value) }
        val belowFeed = feeds.find { it.userId == UserId(stranger2.id.value) }

        assertNotNull(aboveFeed)
        assertNotNull(belowFeed)
        assertTrue(aboveFeed!!.score > belowFeed!!.score)
        assertEquals(0.0, belowFeed.score)
    }

    @Test
    fun `피드 페이징 처리가 정상적으로 동작한다`() = runTest {
        // given: 유사도 임계값(0.7) 이상인 타인 10명 생성
        val me = createUser("me")
        val baseVector = TestVectorFixture.unitVector(1.0f)
        val myKeyword = createKeyword("my", baseVector.toPgVectorString(), me)
        createUserKeyword(me, myKeyword, "내 글")

        val users = (1..10).map { createUser("other$it") }
        users.forEachIndexed { index, user ->
            val sim = 0.95f - (index * 0.02f) // 0.95 ~ 0.77 (모두 0.7 이상)
            val vec = TestVectorFixture.unitVector(sim)
            val kw = createKeyword("kw$index", vec.toPgVectorString(), user)
            createUserKeyword(user, kw, "피드_$index")
        }

        val myUserId = UserId(me.id.value)

        // when: 1페이지 조회 (limit 2)
        val firstPage = repository.getFeeds(
            userId = myUserId,
            cursorScore = null,
            cursorCreatedAt = null,
            cursorUkId = null,
            limit = 2,
        )

        // then: limit 개 조회
        assertEquals(2, firstPage.size)
        val cursorItem = firstPage.last() // 마지막 항목이 커서

        // when: 2페이지 조회
        val secondPage = repository.getFeeds(
            userId = myUserId,
            cursorScore = cursorItem.score,
            cursorCreatedAt = cursorItem.createdAt,
            cursorUkId = cursorItem.userKeywordId,
            limit = 2,
        )

        // then
        println("========================= 2페이지 결과 =========================")
        secondPage.forEach {
            println("ID: ${it.userKeywordId.value}, 점수: ${it.score}, 작성자: ${it.userName}")
        }

        // 2페이지 첫 항목은 커서보다 점수가 낮아야 함
        assertTrue(secondPage[0].score <= cursorItem.score)
        // 1페이지와 중복 없음
        val firstPageIds = firstPage.map { it.userKeywordId.value }.toSet()
        secondPage.forEach {
            assertTrue(it.userKeywordId.value !in firstPageIds, "중복 데이터 발견: ${it.userKeywordId.value}")
        }

        // when: 빈 페이지 검증
        val emptyPage = repository.getFeeds(
            userId = myUserId,
            cursorScore = -1.0,
            cursorCreatedAt = 0L,
            cursorUkId = UserKeywordId(1000L),
            limit = 2,
        )
        assertTrue(emptyPage.isEmpty())
    }

    @Test
    fun `비활성화된 사용자와 사용자 키워드는 조회되지 않는다`() = runTest {
        // given
        val me = createUser("me")
        val friend = createUser("friend")
        val stranger = createUser("stranger")
        val blocked = createUser("blocked")
        val inactiveUser = createUser("inactiveUser")

        val baseVector = TestVectorFixture.unitVector(1.0f)
        val similarVector = TestVectorFixture.unitVector(0.9f)

        val myKeyword = createKeyword("myKeyword", baseVector.toPgVectorString(), me)
        val similarKeyword = createKeyword("similarKeyword", similarVector.toPgVectorString(), me)

        createUserKeyword(me, myKeyword, "내 취향 글")
        createUserKeyword(friend, similarKeyword, "친구의 유사한 글")
        createUserKeyword(stranger, similarKeyword, "남의 유사한 글")
        createUserKeyword(blocked, similarKeyword, "차단된 사용자의 글")
        createUserKeyword(inactiveUser, similarKeyword, "비활성화 사용자의 글")

        createFriends(requesterId = me.id, receiverId = friend.id, status = FriendRequestStatus.ACCEPTED)
        createBlocks(blockerId = me.id, blockedId = blocked.id, reasonId = EntityID(1L, BlockReasons))

        TestDatabaseFactory.dbQuery {
            Users.update({ Users.id eq inactiveUser.id }) { it[Users.isActive] = false }
            UserKeywords.update({ UserKeywords.userId eq inactiveUser.id }) { it[UserKeywords.isActive] = false }
        }

        // when
        val myUserId = UserId(me.id.value)
        val feeds = repository.getFeeds(
            userId = myUserId,
            cursorScore = null,
            cursorCreatedAt = null,
            cursorUkId = null,
            limit = 10,
        )

        // then
        assertTrue(UserId(inactiveUser.id.value) !in feeds.map { it.userId })
        assertTrue(myUserId !in feeds.map { it.userId })
        assertTrue(UserId(blocked.id.value) !in feeds.map { it.userId })
        assertEquals(2, feeds.size) // friend + stranger
    }

    // ==========================================================================================
    // getFallbackFeeds 테스트
    // ==========================================================================================

    @Test
    fun `폴백 피드를 최신순으로 조회한다`() = runTest {
        // given
        val me = createUser("me")
        val user1 = createUser("user1")
        val user2 = createUser("user2")
        val user3 = createUser("user3")

        val vector = TestVectorFixture.unitVector(1.0f)
        val keyword = createKeyword("keyword", vector.toPgVectorString(), me)

        // 순서대로 생성하여 created_at 차이 보장
        val now = Instant.now()
        createUserKeyword(user3, keyword, "오래된 글", createdAt = now.minusSeconds(2))
        createUserKeyword(user2, keyword, "중간 글", createdAt = now.minusSeconds(1))
        createUserKeyword(user1, keyword, "최신 글", createdAt = now)

        // when
        val feeds = repository.getFallbackFeeds(
            userId = UserId(me.id.value),
            cursorCreatedAt = null,
            limit = 10,
        )

        // then: 최신순 정렬 검증
        assertEquals(3, feeds.size)
        assertEquals(user1.id.value, feeds[0].userId.value)
        assertEquals(user2.id.value, feeds[1].userId.value)
        assertEquals(user3.id.value, feeds[2].userId.value)

        // similarity, score 고정값 검증
        feeds.forEach {
            assertEquals(0.0, it.similarity)
            assertEquals(0.0, it.score)
        }
    }

    @Test
    fun `폴백 피드는 내 글과 차단된 사용자 글을 제외한다`() = runTest {
        // given
        val me = createUser("me")
        val stranger = createUser("stranger")
        val blocked = createUser("blocked")

        val vector = TestVectorFixture.unitVector(1.0f)
        val keyword = createKeyword("keyword", vector.toPgVectorString(), me)

        createUserKeyword(me, keyword, "내 글")
        createUserKeyword(stranger, keyword, "타인 글")
        createUserKeyword(blocked, keyword, "차단된 사용자의 글")

        createBlocks(blockerId = me.id, blockedId = blocked.id, reasonId = EntityID(1L, BlockReasons))

        // when
        val myUserId = UserId(me.id.value)
        val feeds = repository.getFallbackFeeds(
            userId = myUserId,
            cursorCreatedAt = null,
            limit = 10,
        )

        // then
        assertEquals(1, feeds.size)
        assertTrue(myUserId !in feeds.map { it.userId })
        assertTrue(UserId(blocked.id.value) !in feeds.map { it.userId })
        assertEquals(stranger.id.value, feeds[0].userId.value)
    }

    @Test
    fun `폴백 피드 페이징이 정상적으로 동작한다`() = runTest {
        // given: 타인 4명 생성 (1페이지 2개 + 2페이지 2개로 소진)
        val me = createUser("me")
        val vector = TestVectorFixture.unitVector(1.0f)
        val keyword = createKeyword("keyword", vector.toPgVectorString(), me)

        val users = (1..4).map { createUser("user$it") }
        val now = Instant.now()
        users.forEachIndexed { index, user ->
            createUserKeyword(
                user = user,
                keyword = keyword,
                description = "글_${user.name}",
                createdAt = now.minusSeconds((users.size - index).toLong()),
            )
        }

        val myUserId = UserId(me.id.value)

        // when: 1페이지 (limit 2)
        val firstPage = repository.getFallbackFeeds(
            userId = myUserId,
            cursorCreatedAt = null,
            limit = 2,
        )

        // then: 1페이지 검증
        assertEquals(2, firstPage.size)
        val cursorItem = firstPage.last()

        // when: 2페이지
        val secondPage = repository.getFallbackFeeds(
            userId = myUserId,
            cursorCreatedAt = cursorItem.createdAt,
            limit = 2,
        )

        // then: 2페이지 검증
        assertEquals(2, secondPage.size)
        // 2페이지 항목은 커서보다 오래된 글이어야 함
        secondPage.forEach {
            assertTrue(it.createdAt < cursorItem.createdAt)
        }
        // 1페이지와 중복 없음
        val firstPageIds = firstPage.map { it.userKeywordId.value }.toSet()
        secondPage.forEach {
            assertTrue(it.userKeywordId.value !in firstPageIds, "중복 데이터 발견: ${it.userKeywordId.value}")
        }

        // when: 빈 페이지 검증 (4명 데이터 소진 후)
        val emptyPage = repository.getFallbackFeeds(
            userId = myUserId,
            cursorCreatedAt = secondPage.last().createdAt,
            limit = 2,
        )

        // then
        assertTrue(emptyPage.isEmpty())
    }

    @Test
    fun `폴백 피드는 비활성화된 사용자 키워드를 제외한다`() = runTest {
        // given
        val me = createUser("me")
        val activeUser = createUser("activeUser")
        val inactiveUser = createUser("inactiveUser")

        val vector = TestVectorFixture.unitVector(1.0f)
        val keyword = createKeyword("keyword", vector.toPgVectorString(), me)

        createUserKeyword(activeUser, keyword, "활성 글")
        createUserKeyword(inactiveUser, keyword, "비활성 글")

        TestDatabaseFactory.dbQuery {
            UserKeywords.update({ UserKeywords.userId eq inactiveUser.id }) { it[UserKeywords.isActive] = false }
        }

        // when
        val feeds = repository.getFallbackFeeds(
            userId = UserId(me.id.value),
            cursorCreatedAt = null,
            limit = 10,
        )

        // then
        assertEquals(1, feeds.size)
        assertEquals(activeUser.id.value, feeds[0].userId.value)
    }

    // ==========================================================================================
    // Helper Functions
    // ==========================================================================================

    private suspend fun createUser(uniqueValue: String): UserEntity = TestDatabaseFactory.dbQuery {
        UserEntity.new {
            this.provider = SocialLoginProvider.GOOGLE
            this.providerId = "pid$uniqueValue"
            this.displayId = "did$uniqueValue"
            this.name = "name$uniqueValue"
            this.introduce = "introduce$uniqueValue"
            this.lastLoginAt = Instant.now()
        }
    }

    private suspend fun createKeyword(
        keyword: String,
        vector: String,
        createdBy: UserEntity,
    ): KeywordEntity = TestDatabaseFactory.dbQuery {
        KeywordEntity.new {
            this.keyword = keyword
            this.embedding = vector
            this.createdBy = createdBy.id
        }
    }

    private suspend fun createUserKeyword(
        user: UserEntity,
        keyword: KeywordEntity,
        description: String,
        createdAt: Instant = Instant.now(),
    ): UserKeywordEntity = TestDatabaseFactory.dbQuery {
        val entity = UserKeywordEntity.new {
            this.userId = user.id
            this.keywordId = keyword.id
            this.description = description
        }
        UserKeywords.update({ UserKeywords.id eq entity.id }) {
            it[UserKeywords.createdAt] = createdAt.toOffsetDateTime()
        }
        entity
    }

    private suspend fun createFriends(
        requesterId: EntityID<Long>,
        receiverId: EntityID<Long>,
        status: FriendRequestStatus,
    ): FriendEntity = TestDatabaseFactory.dbQuery {
        FriendEntity.new {
            this.requesterId = requesterId
            this.receiverId = receiverId
            this.status = status
        }
    }

    private suspend fun createBlocks(
        blockerId: EntityID<Long>,
        blockedId: EntityID<Long>,
        reasonId: EntityID<Long>,
    ): BlockEntity = TestDatabaseFactory.dbQuery {
        BlockEntity.new {
            this.blockerId = blockerId
            this.blockedId = blockedId
            this.reasonId = reasonId
        }
    }
}
