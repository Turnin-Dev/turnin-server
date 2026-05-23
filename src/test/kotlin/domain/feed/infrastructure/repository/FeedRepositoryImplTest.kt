package com.turnin.domain.feed.infrastructure.repository

import com.turnin.common.db.schema.BlockEntity
import com.turnin.common.db.schema.BlockReasons
import com.turnin.common.db.schema.FriendEntity
import com.turnin.common.db.schema.KeywordEntity
import com.turnin.common.db.schema.UserEntity
import com.turnin.common.db.schema.UserKeywordEntity
import com.turnin.common.db.schema.UserKeywords
import com.turnin.common.db.schema.Users
import com.turnin.common.ml.keywordCategory.KeywordCategory
import com.turnin.common.model.FriendRequestStatus
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.common.util.toOffsetDateTime
import com.turnin.domain.discover.util.TestVectorFixture
import com.turnin.domain.discover.util.TestVectorFixture.toPgVectorString
import com.turnin.util.db.PostgresRule
import com.turnin.util.db.TestDatabaseFactory
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

        val myKeyword = createKeyword("myKeyword", category = KeywordCategory.TECH, createdBy = me)
        val sameCategory = createKeyword("sameCateKeyword", category = KeywordCategory.TECH, createdBy = me)

        createUserKeyword(me, myKeyword, "내 취향 글")
        createUserKeyword(friend, sameCategory, "친구의 같은 카테고리 글") // friend_pool: 점수 최상위
        createUserKeyword(stranger, sameCategory, "남의 같은 카테고리 글") // similar_pool: 점수 중간
        createUserKeyword(blocked, sameCategory, "차단된 사용자의 글") // 차단 -> 조회 X

        createFriends(requesterId = me.id, receiverId = friend.id, status = FriendRequestStatus.ACCEPTED)
        createBlocks(blockerId = me.id, blockedId = blocked.id, reasonId = EntityID(1L, BlockReasons))

        // when
        val myUserId = UserId(me.id.value)
        val feeds = repository.getFeeds(
            userId = myUserId,
            cursorScore = null,
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

        // 순서 검증: 1등(친구, friend_pool) -> 2등(남, similar_pool)
        assertEquals(2, feeds.size)
        val firstFeed = feeds[0]
        val secondFeed = feeds[1]

        // 1등: 친구 보너스(100) = 100
        assertEquals(friend.id.value, firstFeed.userId.value)
        assertEquals(100.0, firstFeed.score)

        // 2등: 카테고리 유사도(0.5 * 50 = 25)만
        assertEquals(stranger.id.value, secondFeed.userId.value)
        assertEquals(25.0, secondFeed.score)
        assertTrue(firstFeed.score > secondFeed.score)
    }

    @Test
    fun `friend_pool이 similar_pool보다 우선순위가 높다`() = runTest {
        // given: 친구 글은 friend_pool(보너스 100), 타인 글은 similar_pool(보너스 없음)
        val me = createUser("me")
        val friend = createUser("friend")
        val stranger = createUser("stranger")

        val myKeyword = createKeyword("myKeyword", category = KeywordCategory.TECH, createdBy = me)
        val sameCategory = createKeyword("sameCateKeyword", category = KeywordCategory.TECH, createdBy = me)

        createUserKeyword(me, myKeyword, "내 글")
        createUserKeyword(friend, sameCategory, "친구 글")
        createUserKeyword(stranger, sameCategory, "타인 글")

        createFriends(requesterId = me.id, receiverId = friend.id, status = FriendRequestStatus.ACCEPTED)

        // when
        val feeds = repository.getFeeds(
            userId = UserId(me.id.value),
            cursorScore = null,
            cursorUkId = null,
            limit = 10,
        )

        // then: 친구 글이 friend 보너스(100)로 1등
        assertEquals(2, feeds.size)
        assertEquals(friend.id.value, feeds[0].userId.value)
        assertEquals(stranger.id.value, feeds[1].userId.value)
        assertTrue(feeds[0].score > feeds[1].score)
    }

    @Test
    fun `친구는 카테고리 무관하게 score가 100이다`() = runTest {
        val me = createUser("me")
        val friend = createUser("friend")
        val myKeyword = createKeyword("myKeyword", category = KeywordCategory.TECH, createdBy = me)
        val differentCategory = createKeyword("diffKeyword", category = KeywordCategory.FOOD, createdBy = me)

        createUserKeyword(me, myKeyword, "내 글")
        createUserKeyword(friend, differentCategory, "친구 글 (다른 카테고리)")
        createFriends(requesterId = me.id, receiverId = friend.id, status = FriendRequestStatus.ACCEPTED)

        val feeds = repository.getFeeds(
            userId = UserId(me.id.value),
            cursorScore = null,
            cursorUkId = null,
            limit = 10,
        )

        assertEquals(1, feeds.size)
        assertEquals(100.0, feeds[0].score) // 카테고리 달라도 100
    }

    @Test
    fun `카테고리가 없는 키워드는 similar_pool에서 제외되고 fallback_pool로 처리된다`() = runTest {
        // given
        val me = createUser("me")
        val stranger1 = createUser("stranger1")
        val stranger2 = createUser("stranger2")

        // 내 키워드: 카테고리 있음
        val myKeyword = createKeyword("myKeyword", category = KeywordCategory.TECH, createdBy = me)
        // stranger1: 동일 카테고리 -> similar_pool (score = 0.5 * 50 = 25)
        val sameCategoryKeyword = createKeyword("sameCateKeyword", category = KeywordCategory.TECH, createdBy = me)
        // stranger2: 카테고리 없음 -> fallback_pool (score = 0)
        val noCategoryKeyword = createKeyword("noCateKeyword", category = null, createdBy = me)

        createUserKeyword(me, myKeyword, "내 글")
        createUserKeyword(stranger1, sameCategoryKeyword, "같은 카테고리 글")
        createUserKeyword(stranger2, noCategoryKeyword, "카테고리 없는 글")

        // when
        val feeds = repository.getFeeds(
            userId = UserId(me.id.value),
            cursorScore = null,
            cursorUkId = null,
            limit = 10,
        )

        // then
        val feed1 = feeds.find { it.userId == UserId(stranger1.id.value) }
        val feed2 = feeds.find { it.userId == UserId(stranger2.id.value) }

        assertNotNull(feed1)
        assertNotNull(feed2)
        assertEquals(25.0, feed1!!.score) // similar_pool: 0.5 * 50 = 25
        assertEquals(0.0, feed2!!.score) // fallback_pool: score = 0
        assertTrue(feed1.score > feed2.score)
    }

    @Test
    fun `내 키워드에 카테고리가 없으면 similar_pool이 비어 fallback_pool만 반환된다`() = runTest {
        // given: 내 키워드의 카테고리가 null → my_categories 비어있음
        val me = createUser("me")
        val stranger = createUser("stranger")

        val myKeyword = createKeyword("myKeyword", category = null, createdBy = me)
        val strangerKeyword = createKeyword("strangerKeyword", category = KeywordCategory.TECH, createdBy = me)

        createUserKeyword(me, myKeyword, "내 글")
        createUserKeyword(stranger, strangerKeyword, "타인 글")

        // when
        val feeds = repository.getFeeds(
            userId = UserId(me.id.value),
            cursorScore = null,
            cursorUkId = null,
            limit = 10,
        )

        // then: fallback_pool으로만 처리 → score = 0, similarity = 0
        assertEquals(1, feeds.size)
        assertEquals(stranger.id.value, feeds[0].userId.value)
        assertEquals(0.0, feeds[0].score)
        assertEquals(0.0, feeds[0].similarity)
    }

    @Test
    fun `피드 페이징 처리가 정상적으로 동작한다`() = runTest {
        // given: 같은 카테고리의 타인 10명 생성
        val me = createUser("me")
        val myKeyword = createKeyword("my", category = KeywordCategory.TECH, createdBy = me)
        createUserKeyword(me, myKeyword, "내 글")

        val users = (1..10).map { createUser("other$it") }
        users.forEachIndexed { index, user ->
            val kw = createKeyword("kw_${user.name}", category = KeywordCategory.TECH, createdBy = me)
            createUserKeyword(
                user = user,
                keyword = kw,
                description = "피드_${user.name}",
                createdAt = Instant.now().minusSeconds(index.toLong() + 1),
            )
        }

        val myUserId = UserId(me.id.value)

        // when: 1페이지 조회 (limit 2)
        val firstPage = repository.getFeeds(
            userId = myUserId,
            cursorScore = null,
            cursorUkId = null,
            limit = 2,
        )

        // then: limit 개 조회
        assertEquals(2, firstPage.size)
        val cursorItem = firstPage.last()

        // when: 2페이지 조회
        val secondPage = repository.getFeeds(
            userId = myUserId,
            cursorScore = cursorItem.score,
            cursorUkId = cursorItem.userKeywordId,
            limit = 2,
        )

        // then
        println("========================= 2페이지 결과 =========================")
        secondPage.forEach {
            println("ID: ${it.userKeywordId.value}, 점수: ${it.score}, 작성자: ${it.userName}")
        }

        // 2페이지 첫 항목은 커서보다 점수가 낮거나 같아야 함
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
            cursorUkId = UserKeywordId(1000L),
            limit = 2,
        )
        assertTrue(emptyPage.isEmpty())
    }

    @Test
    fun `같은 시간에 등록된 피드도 uk_id로 페이징이 정상 동작한다`() = runTest {
        val me = createUser("me")
        val myKeyword = createKeyword("my", category = KeywordCategory.TECH, createdBy = me)
        createUserKeyword(me, myKeyword, "내 글")

        val fixedTime = Instant.ofEpochSecond(Instant.now().epochSecond - 10)
        val users = (1..10).map { createUser("other$it") }
        users.forEach { user ->
            val kw = createKeyword("kw_${user.name}", category = KeywordCategory.TECH, createdBy = me)
            createUserKeyword(
                user = user,
                keyword = kw,
                description = "피드_${user.name}",
                createdAt = fixedTime,
            )
        }

        val myUserId = UserId(me.id.value)

        val firstPage = repository.getFeeds(
            userId = myUserId,
            cursorScore = null,
            cursorUkId = null,
            limit = 2,
        )
        assertEquals(2, firstPage.size)
        val cursorItem = firstPage.last()

        val secondPage = repository.getFeeds(
            userId = myUserId,
            cursorScore = cursorItem.score,
            cursorUkId = cursorItem.userKeywordId,
            limit = 2,
        )

        assertEquals(2, secondPage.size)
        val firstPageIds = firstPage.map { it.userKeywordId.value }.toSet()
        secondPage.forEach {
            assertTrue(it.userKeywordId.value !in firstPageIds, "중복 데이터 발견: ${it.userKeywordId.value}")
        }
    }

    @Test
    fun `비활성화된 사용자와 사용자 키워드는 조회되지 않는다`() = runTest {
        // given
        val me = createUser("me")
        val friend = createUser("friend")
        val stranger = createUser("stranger")
        val blocked = createUser("blocked")
        val inactiveUser = createUser("inactiveUser")

        val myKeyword = createKeyword("myKeyword", category = KeywordCategory.TECH, createdBy = me)
        val sameCategory = createKeyword("sameCateKeyword", category = KeywordCategory.TECH, createdBy = me)

        createUserKeyword(me, myKeyword, "내 취향 글")
        createUserKeyword(friend, sameCategory, "친구의 글")
        createUserKeyword(stranger, sameCategory, "남의 글")
        createUserKeyword(blocked, sameCategory, "차단된 사용자의 글")
        createUserKeyword(inactiveUser, sameCategory, "비활성화 사용자의 글")

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
    fun `폴백 피드를 uk_id 기준 내림차순으로 조회한다`() = runTest {
        // given
        val me = createUser("me")
        val user1 = createUser("user1")
        val user2 = createUser("user2")
        val user3 = createUser("user3")

        val keyword = createKeyword("keyword", category = KeywordCategory.TECH, createdBy = me)

        // 순서대로 생성하여 uk_id 차이 보장
        val now = Instant.now()
        createUserKeyword(user3, keyword, "오래된 글", createdAt = now.minusSeconds(2))
        createUserKeyword(user2, keyword, "중간 글", createdAt = now.minusSeconds(1))
        createUserKeyword(user1, keyword, "최신 글", createdAt = now)

        // when
        val feeds = repository.getFallbackFeeds(
            userId = UserId(me.id.value),
            cursorUkId = null,
            limit = 10,
        )

        // then: uk_id 내림차순 정렬 검증 (생성순 == id순이므로 최신 = 가장 큰 id)
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

        val keyword = createKeyword("keyword", category = KeywordCategory.TECH, createdBy = me)

        createUserKeyword(me, keyword, "내 글")
        createUserKeyword(stranger, keyword, "타인 글")
        createUserKeyword(blocked, keyword, "차단된 사용자의 글")

        createBlocks(blockerId = me.id, blockedId = blocked.id, reasonId = EntityID(1L, BlockReasons))

        // when
        val myUserId = UserId(me.id.value)
        val feeds = repository.getFallbackFeeds(
            userId = myUserId,
            cursorUkId = null,
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
        val keyword = createKeyword("keyword", category = KeywordCategory.TECH, createdBy = me)

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
            cursorUkId = null,
            limit = 2,
        )

        // then: 1페이지 검증
        assertEquals(2, firstPage.size)
        val cursorItem = firstPage.last()

        // when: 2페이지
        val secondPage = repository.getFallbackFeeds(
            userId = myUserId,
            cursorUkId = cursorItem.userKeywordId,
            limit = 2,
        )

        // then: 2페이지 검증
        assertEquals(2, secondPage.size)
        // 2페이지 항목은 커서보다 uk_id가 작아야 함
        secondPage.forEach {
            assertTrue(it.userKeywordId.value < cursorItem.userKeywordId.value)
        }
        // 1페이지와 중복 없음
        val firstPageIds = firstPage.map { it.userKeywordId.value }.toSet()
        secondPage.forEach {
            assertTrue(it.userKeywordId.value !in firstPageIds, "중복 데이터 발견: ${it.userKeywordId.value}")
        }

        // when: 빈 페이지 검증 (4명 데이터 소진 후)
        val emptyPage = repository.getFallbackFeeds(
            userId = myUserId,
            cursorUkId = UserKeywordId(secondPage.last().userKeywordId.value),
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

        val keyword = createKeyword("keyword", category = KeywordCategory.TECH, createdBy = me)

        createUserKeyword(activeUser, keyword, "활성 글")
        createUserKeyword(inactiveUser, keyword, "비활성 글")

        TestDatabaseFactory.dbQuery {
            UserKeywords.update({ UserKeywords.userId eq inactiveUser.id }) { it[UserKeywords.isActive] = false }
        }

        // when
        val feeds = repository.getFallbackFeeds(
            userId = UserId(me.id.value),
            cursorUkId = null,
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
        category: KeywordCategory?,
        createdBy: UserEntity,
    ): KeywordEntity = TestDatabaseFactory.dbQuery {
        KeywordEntity.new {
            this.keyword = keyword
            this.embedding = TestVectorFixture.orthogonalVector().toPgVectorString()
            this.category = category
            this.categorySimilarity = null
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
