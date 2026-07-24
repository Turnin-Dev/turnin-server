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
        assertEquals(1, result.feedsRows.size)
        assertEquals(
            friendUk.id.value,
            result.feedsRows[0]
                .feed.userKeywordId.value,
        )
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
        val resultIds = result.feedsRows.map { it.feed.userKeywordId.value }.toSet()
        assertTrue(friendUk.id.value in resultIds)
        assertTrue(strangerUk.id.value in resultIds)
        assertEquals(2, result.feedsRows.size)
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
        assertEquals(2, firstResult.feedsRows.size)

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
        assertEquals(2, sameSessionResult.feedsRows.size)
        assertTrue(lateAuthor.id.value !in sameSessionResult.feedsRows.map { it.feed.userId.value })

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
        assertEquals(3, newSessionResult.feedsRows.size)
        assertTrue(lateAuthor.id.value in newSessionResult.feedsRows.map { it.feed.userId.value })
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

        while (page.feedsRows.isNotEmpty()) {
            collected += page.feedsRows.map { it.feed.userKeywordId.value }
            lastShuffleKey = page.feedsRows.last().shuffleKey
            lastUkId = page.feedsRows
                .last()
                .feed.userKeywordId.value

            if (page.feedsRows.size < 2) break // 청크 소진

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
        assertEquals(3, firstChunk.feedsRows.size)
        assertEquals(3, firstChunk.windowFetchedCount)
        assertNotNull(firstChunk.windowMinUkId)

        // when: 2번째 청크 조회 (windowAnchorId = 첫 청크의 windowMinUkId, 커서 리셋)
        val secondChunk = repository.getAllFeeds(
            userId = myUserId,
            seed = "seed-chunk-1",
            sessionMaxId = firstChunk.sessionMaxId,
            windowAnchorId = UserKeywordId(firstChunk.windowMinUkId),
            lastShuffleKey = null,
            lastUkId = null,
            windowSize = 3,
            limit = 3,
        )

        // then: 남은 2개가 조회되고 첫 청크와 중복 없음
        assertEquals(2, secondChunk.feedsRows.size)
        val firstChunkIds = firstChunk.feedsRows.map { it.feed.userKeywordId.value }.toSet()
        secondChunk.feedsRows.forEach {
            assertTrue(
                it.feed.userKeywordId.value !in firstChunkIds,
                "중복 데이터 발견: ${it.feed.userKeywordId.value}",
            )
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
        assertTrue(thirdChunk.feedsRows.isEmpty())
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
            first.feedsRows.map { it.feed.userKeywordId.value },
            second.feedsRows.map { it.feed.userKeywordId.value },
        )
    }

    // ==========================================================================================
    // 엣지 케이스
    // ==========================================================================================

    // TODO: [페이지네이션 버그 티켓](https://peekr-app.atlassian.net/browse/PK-147) 해결 후 주석 해제
    // 버그 재현: 청크 내 커서가 마지막 지점에 도달하면 windowFetchedCount가 0으로 잘못 반환되어,
    // 실제로는 남아있는 다음 청크 데이터에 도달하지 못한다
//    @Test
//    fun `커서가 청크의 끝을 가리키면 다음 청크에 데이터가 남아있어도 소진된 것으로 잘못 판단된다`() = runTest {
//        // given: windowSize=5보다 훨씬 많은 8개 글 (1번째 청크 5개 + 2번째 청크 3개)
//        val me = createUser("me")
//        val keyword = createKeyword("keyword", createdBy = me)
//        val users = (1..8).map { createUser("user$it") }
//        val now = Instant.now()
//        users.forEachIndexed { index, user ->
//            createUserKeyword(
//                user = user,
//                keyword = keyword,
//                description = "글_${user.name}",
//                createdAt = now.minusSeconds((8 - index).toLong()),
//            )
//        }
//
//        val myUserId = UserId(me.id.value)
//        val seed = "seed-boundary-1"
//
//        // when: 1번째 청크(5개)를 한 번에 조회해서 마지막 행의 커서 값을 얻음
//        val firstPage = repository.getAllFeeds(
//            userId = myUserId,
//            seed = seed,
//            sessionMaxId = null,
//            windowAnchorId = null,
//            lastShuffleKey = null,
//            lastUkId = null,
//            windowSize = 5,
//            limit = 5,
//        )
//        assertEquals(5, firstPage.feedsRows.size)
//        assertEquals(5, firstPage.windowFetchedCount)
//        assertNotNull(firstPage.windowMinUkId) // 다음 청크로 넘어가기 위한 anchor
//
//        val lastRow = firstPage.feedsRows.last()
//
//        // when: 같은 청크(anchor 안 옮김) 내에서, 마지막 행을 커서로 삼아 재조회
//        // (실제로는 이 청크에 더 볼 게 없으므로 windowAnchorId=firstPage.windowMinUkId로 다음 청크로 넘어가야 하는 시점)
//        val boundaryPage = repository.getAllFeeds(
//            userId = myUserId,
//            seed = seed,
//            sessionMaxId = firstPage.sessionMaxId,
//            windowAnchorId = null,
//            lastShuffleKey = lastRow.shuffleKey,
//            lastUkId = lastRow.feed.userKeywordId.value,
//            windowSize = 5,
//            limit = 5,
//        )
//
//        // then: window_pool엔 여전히 5개(firstPage.windowFetchedCount와 동일)가 있었으므로
//        // windowFetchedCount는 0이 아니라 5, windowMinUkId도 firstPage와 동일해야 함
//        assertTrue(boundaryPage.feedsRows.isEmpty())
//        assertEquals(5, boundaryPage.windowFetchedCount) // 현재 실제값 0 -> 이 줄에서 실패해야 정상 (버그 증명)
//        assertEquals(4L, boundaryPage.windowMinUkId) // 현재 실제값 null -> 이 줄에서 실패해야 정상
//        assertEquals(firstPage.sessionMaxId, boundaryPage.sessionMaxId) // 현재 실제값 null -> 이 줄에서 실패해야 정상
//    }

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
