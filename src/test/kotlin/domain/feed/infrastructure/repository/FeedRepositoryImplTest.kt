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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.update
import org.junit.Rule

class FeedRepositoryImplTest {
    @get:Rule
    val dbRule = PostgresRule()

    private val repository = FeedRepositoryImpl()

    // ==========================================================================================
    // getFriendFeeds 테스트
    // ==========================================================================================

    @Test
    fun `친구 피드는 친구가 작성한 활성 글만 조회하고 자신, 차단된 사용자, 비활성 사용자는 제외한다`() = runTest {
        // given
        val me = createUser("me")
        val friend = createUser("friend")
        val inactiveFriend = createUser("inactiveFriend")
        val stranger = createUser("stranger")
        val blocked = createUser("blocked")

        val keyword = createKeyword("keyword", createdBy = me)

        createUserKeyword(me, keyword, "내 글")
        val friendUk = createUserKeyword(friend, keyword, "친구 글")
        createUserKeyword(inactiveFriend, keyword, "비활성 친구 글")
        createUserKeyword(stranger, keyword, "타인 글") // 친구 아님 -> 제외
        createUserKeyword(blocked, keyword, "차단된 친구 글")

        createFriends(requesterId = me.id, receiverId = friend.id, status = FriendRequestStatus.ACCEPTED)
        createFriends(requesterId = me.id, receiverId = inactiveFriend.id, status = FriendRequestStatus.ACCEPTED)
        createFriends(requesterId = me.id, receiverId = blocked.id, status = FriendRequestStatus.ACCEPTED)
        createBlocks(blockerId = me.id, blockedId = blocked.id, reasonId = EntityID(1L, BlockReasons))

        TestDatabaseFactory.dbQuery {
            Users.update({ Users.id eq inactiveFriend.id }) { it[Users.isActive] = false }
            UserKeywords.update({ UserKeywords.userId eq inactiveFriend.id }) { it[UserKeywords.isActive] = false }
        }

        // when
        val result = repository.getFriendFeeds(
            userId = UserId(me.id.value),
            seed = "seed-friend-1",
            sessionMaxId = null,
            windowAnchorId = null,
            lastShuffleKey = null,
            lastUkId = null,
            windowSize = 100,
            limit = 10,
        )

        // then
        assertEquals(1, result.feeds.size)
        assertEquals(friendUk.id.value, result.feeds[0].userKeywordId.value)
    }

    // ==========================================================================================
    // getAllFeeds 테스트
    // ==========================================================================================

    @Test
    fun `전체 피드는 자신과 차단된 사용자, 비활성 사용자를 제외한 모든 활성 글을 조회한다`() = runTest {
        // given
        val me = createUser("me")
        val friend = createUser("friend")
        val stranger = createUser("stranger")
        val blocked = createUser("blocked")
        val inactiveUser = createUser("inactiveUser")

        val keyword = createKeyword("keyword", createdBy = me)

        createUserKeyword(me, keyword, "내 글")
        val friendUk = createUserKeyword(friend, keyword, "친구 글")
        val strangerUk = createUserKeyword(stranger, keyword, "타인 글")
        createUserKeyword(blocked, keyword, "차단된 사용자 글")
        createUserKeyword(inactiveUser, keyword, "비활성 사용자 글")

        createFriends(requesterId = me.id, receiverId = friend.id, status = FriendRequestStatus.ACCEPTED)
        createBlocks(blockerId = me.id, blockedId = blocked.id, reasonId = EntityID(1L, BlockReasons))

        TestDatabaseFactory.dbQuery {
            Users.update({ Users.id eq inactiveUser.id }) { it[Users.isActive] = false }
            UserKeywords.update({ UserKeywords.userId eq inactiveUser.id }) { it[UserKeywords.isActive] = false }
        }

        // when
        val result = repository.getAllFeeds(
            userId = UserId(me.id.value),
            seed = "seed-all-1",
            sessionMaxId = null,
            windowAnchorId = null,
            lastShuffleKey = null,
            lastUkId = null,
            windowSize = 100,
            limit = 10,
        )

        // then: 친구 여부와 무관하게 활성 상태인 타인 글은 전부 포함
        val resultIds = result.feeds.map { it.userKeywordId.value }.toSet()
        assertTrue(friendUk.id.value in resultIds)
        assertTrue(strangerUk.id.value in resultIds)
        assertEquals(2, result.feeds.size)
    }

    // ==========================================================================================
    // sessionMaxId 테스트
    // ==========================================================================================

    @Test
    fun `sessionMaxId 이후 작성된 글은 같은 세션에서 조회되지 않는다`() = runTest {
        // given: 세션 시작 전 글 2개
        val me = createUser("me")
        val keyword = createKeyword("keyword", createdBy = me)

        val author1 = createUser("author1")
        val author2 = createUser("author2")
        createUserKeyword(author1, keyword, "세션 이전 글 1")
        createUserKeyword(author2, keyword, "세션 이전 글 2")

        // when: 첫 조회로 세션을 시작하고 sessionMaxId를 확정
        val firstResult = repository.getAllFeeds(
            userId = UserId(me.id.value),
            seed = "seed-session-1",
            sessionMaxId = null,
            windowAnchorId = null,
            lastShuffleKey = null,
            lastUkId = null,
            windowSize = 100,
            limit = 100,
        )
        assertNotNull(firstResult.sessionMaxId)
        assertEquals(2, firstResult.feeds.size)

        // 세션 시작 이후 새 글 작성
        val lateAuthor = createUser("lateAuthor")
        createUserKeyword(lateAuthor, keyword, "세션 시작 이후 새 글")

        // then: 같은 sessionMaxId로 재조회하면 새 글이 안 보여야 함
        val sameSessionResult = repository.getAllFeeds(
            userId = UserId(me.id.value),
            seed = "seed-session-1",
            sessionMaxId = firstResult.sessionMaxId,
            windowAnchorId = null,
            lastShuffleKey = null,
            lastUkId = null,
            windowSize = 100,
            limit = 100,
        )
        assertEquals(2, sameSessionResult.feeds.size)
        assertTrue(lateAuthor.id.value !in sameSessionResult.feeds.map { it.userId.value })

        // then: 세션을 새로 시작(sessionMaxId = null)하면 새 글이 보여야 함
        val newSessionResult = repository.getAllFeeds(
            userId = UserId(me.id.value),
            seed = "seed-session-2",
            sessionMaxId = null,
            windowAnchorId = null,
            lastShuffleKey = null,
            lastUkId = null,
            windowSize = 100,
            limit = 100,
        )
        assertEquals(3, newSessionResult.feeds.size)
        assertTrue(lateAuthor.id.value in newSessionResult.feeds.map { it.userId.value })
    }

    // ==========================================================================================
    // 청크 내부 커서 페이지네이션 테스트
    // ==========================================================================================

    @Test
    fun `같은 청크 내에서 커서로 페이징하면 중복 없이 전부 조회된다`() = runTest {
        // given: 한 청크(windowSize=5)에 다 들어가는 5개 글
        val me = createUser("me")
        val keyword = createKeyword("keyword", createdBy = me)
        val users = (1..5).map { createUser("user$it") }
        val now = Instant.now()
        users.forEachIndexed { index, user ->
            createUserKeyword(
                user = user,
                keyword = keyword,
                description = "글_${user.name}",
                createdAt = now.minusSeconds((5 - index).toLong()),
            )
        }

        val myUserId = UserId(me.id.value)
        val seed = "seed-cursor-1"
        val collected = mutableListOf<Long>()

        // when: limit 2씩 커서를 이어가며 전부 소진할 때까지 조회
        var lastShuffleKey: Int? = null
        var lastUkId: Long? = null
        var page = repository.getAllFeeds(
            userId = myUserId,
            seed = seed,
            sessionMaxId = null,
            windowAnchorId = null,
            lastShuffleKey = lastShuffleKey,
            lastUkId = lastUkId,
            windowSize = 5,
            limit = 2,
        )
        val sessionMaxId = page.sessionMaxId

        while (page.feeds.isNotEmpty()) {
            collected += page.feeds.map { it.userKeywordId.value }
            lastShuffleKey = page.lastShuffleKey
            lastUkId = page.lastUkId

            if (page.feeds.size < 2) break // 청크 소진

            page = repository.getAllFeeds(
                userId = myUserId,
                seed = seed,
                sessionMaxId = sessionMaxId,
                windowAnchorId = null,
                lastShuffleKey = lastShuffleKey,
                lastUkId = lastUkId,
                windowSize = 5,
                limit = 2,
            )
        }

        // then: 중복 없이 5개 전부 조회됨
        assertEquals(5, collected.size)
        assertEquals(5, collected.toSet().size)
    }

    // ==========================================================================================
    // 청크(window) 전환 테스트
    // ==========================================================================================

    @Test
    fun `청크가 소진되면 windowAnchorId로 다음 청크로 이동하고 중복 없이 조회된다`() = runTest {
        // given: windowSize=3보다 많은 5개 글 (첫 청크 3개 + 다음 청크 2개)
        val me = createUser("me")
        val keyword = createKeyword("keyword", createdBy = me)
        val users = (1..5).map { createUser("user$it") }
        val now = Instant.now()
        users.forEachIndexed { index, user ->
            createUserKeyword(
                user = user,
                keyword = keyword,
                description = "글_${user.name}",
                createdAt = now.minusSeconds((5 - index).toLong()),
            )
        }

        val myUserId = UserId(me.id.value)

        // when: 1번째 청크 조회 (windowSize=3, limit=3 -> 청크를 한 번에 다 가져옴)
        val firstChunk = repository.getAllFeeds(
            userId = myUserId,
            seed = "seed-chunk-1",
            sessionMaxId = null,
            windowAnchorId = null,
            lastShuffleKey = null,
            lastUkId = null,
            windowSize = 3,
            limit = 3,
        )
        assertEquals(3, firstChunk.feeds.size)
        assertEquals(3, firstChunk.windowFetchedCount)
        assertNotNull(firstChunk.windowMinUkId)

        // when: 2번째 청크 조회 (windowAnchorId = 첫 청크의 windowMinUkId, 커서 리셋)
        val secondChunk = repository.getAllFeeds(
            userId = myUserId,
            seed = "seed-chunk-1",
            sessionMaxId = firstChunk.sessionMaxId,
            windowAnchorId = UserKeywordId(firstChunk.windowMinUkId!!),
            lastShuffleKey = null,
            lastUkId = null,
            windowSize = 3,
            limit = 3,
        )

        // then: 남은 2개가 조회되고 첫 청크와 중복 없음
        assertEquals(2, secondChunk.feeds.size)
        val firstChunkIds = firstChunk.feeds.map { it.userKeywordId.value }.toSet()
        secondChunk.feeds.forEach {
            assertTrue(it.userKeywordId.value !in firstChunkIds, "중복 데이터 발견: ${it.userKeywordId.value}")
        }

        // when: 더 이상 조회할 청크가 없는 상태
        val thirdChunk = repository.getAllFeeds(
            userId = myUserId,
            seed = "seed-chunk-1",
            sessionMaxId = firstChunk.sessionMaxId,
            windowAnchorId = UserKeywordId(secondChunk.windowMinUkId!!),
            lastShuffleKey = null,
            lastUkId = null,
            windowSize = 3,
            limit = 3,
        )

        // then
        assertTrue(thirdChunk.feeds.isEmpty())
        assertEquals(0, thirdChunk.windowFetchedCount)
        assertNull(thirdChunk.windowMinUkId)
    }

    // ==========================================================================================
    // 셔플 결정성 테스트
    // ==========================================================================================

    @Test
    fun `같은 seed로 같은 청크를 조회하면 항상 동일한 순서가 나온다`() = runTest {
        // given
        val me = createUser("me")
        val keyword = createKeyword("keyword", createdBy = me)
        val users = (1..8).map { createUser("user$it") }
        val now = Instant.now()
        users.forEachIndexed { index, user ->
            createUserKeyword(
                user = user,
                keyword = keyword,
                description = "글_${user.name}",
                createdAt = now.minusSeconds((8 - index).toLong()),
            )
        }

        val myUserId = UserId(me.id.value)

        // when: 같은 seed로 두 번 조회
        val first = repository.getAllFeeds(
            userId = myUserId,
            seed = "fixed-seed",
            sessionMaxId = null,
            windowAnchorId = null,
            lastShuffleKey = null,
            lastUkId = null,
            windowSize = 100,
            limit = 100,
        )
        val second = repository.getAllFeeds(
            userId = myUserId,
            seed = "fixed-seed",
            sessionMaxId = first.sessionMaxId,
            windowAnchorId = null,
            lastShuffleKey = null,
            lastUkId = null,
            windowSize = 100,
            limit = 100,
        )

        // then: 완전히 동일한 순서
        assertEquals(
            first.feeds.map { it.userKeywordId.value },
            second.feeds.map { it.userKeywordId.value },
        )
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
        createdBy: UserEntity,
        category: KeywordCategory? = KeywordCategory.TECH,
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
