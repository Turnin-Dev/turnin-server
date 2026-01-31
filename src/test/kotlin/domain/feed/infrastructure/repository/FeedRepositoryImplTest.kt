package com.peekr.domain.feed.infrastructure.repository

import com.peekr.common.db.schema.BlockEntity
import com.peekr.common.db.schema.BlockReasons
import com.peekr.common.db.schema.FriendEntity
import com.peekr.common.db.schema.KeywordEntity
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.db.schema.UserKeywordEntity
import com.peekr.common.model.FriendRequestStatus
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.discover.util.TestVectorFixture
import com.peekr.domain.discover.util.TestVectorFixture.toPgVectorString
import com.peekr.util.db.PostgresRule
import com.peekr.util.db.TestDatabaseFactory
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.dao.id.EntityID
import org.junit.Rule
import org.junit.Test

class FeedRepositoryImplTest {
    @get:Rule
    val dbRule = PostgresRule()

    private val repository = FeedRepositoryImpl()

    @Test
    fun `첫 페이지를 정상적으로 조회한다`() = runTest {
        // ------------------------------ given: 데이터 준비 ------------------------------
        // 사용자 생성
        val me = createUser("me")
        val friend = createUser("friend")
        val stranger = createUser("stranger")
        val blocked = createUser("blocked")
        // 벡터 생성
        val baseVector = TestVectorFixture.unitVector(1.0f)
        val similarVector = TestVectorFixture.unitVector(0.9f)
        val diffVector = TestVectorFixture.orthogonalVector()
        // 키워드 생성
        val myKeyword = createKeyword("myKeyword", baseVector.toPgVectorString(), me)
        val similarKeyword = createKeyword("similarKeyword", similarVector.toPgVectorString(), me)
        val diffKeyword = createKeyword("diffKeyword", diffVector.toPgVectorString(), me)
        // 사용자 키워드 생성
        // 1. 나: 내 취향 키워드 등록 (기준)
        createUserKeyword(me, myKeyword, "내 취향 글")
        // 2. 친구: 유사 키워드 등록 (친구 O + 취향 O -> 점수 취상위 예상)
        createUserKeyword(friend, similarKeyword, "친구의 유사한 글")
        // 3. 남: 유사 키워드 글 등록 (친구 X + 취향 O -> 점수 중간 예상)
        createUserKeyword(stranger, similarKeyword, "남의 유사한 글")
        // 4. 남: 완전 다른 키워드 등록 (친구 X + 취향 X -> 점수 취하위 예상)
        createUserKeyword(stranger, diffKeyword, "남의 글")
        // 5. 차단된 사용자: 유사 키워드 글 등록 (친구 X + 취향 O -> 하지만 차단이기 때문에 조회 X)
        createUserKeyword(blocked, similarKeyword, "차단된 사용자의 글")
        // 친구 관계 생성 (나 <-> 친구)
        createFriends(
            requesterId = me.id,
            receiverId = friend.id,
            status = FriendRequestStatus.ACCEPTED,
        )
        // 차단 관계 생성
        createBlocks(
            blockerId = me.id,
            blockedId = blocked.id,
            reasonId = EntityID(1L, BlockReasons),
        )

        // ------------------------------ when: 피드 조회 ------------------------------
        val myUserId = UserId(me.id.value)
        val feeds = repository.getFeeds(
            userId = myUserId,
            cursorScore = null,
            cursorCreatedAt = null,
            cursorUkId = null,
            limit = 10,
        )

        // ------------------------------ then ------------------------------
        println("==========================================================================================")
        println("조회된 피드 개수: ${feeds.size}")
        feeds.forEach {
            println("작성자: ${it.userName}, 점수: ${it.score}, 유사도: ${it.similarity}, 키워드: ${it.keyword}")
        }
        println("==========================================================================================")

        // 필터링 검증: 내 글 제외, 차단된 사용자 글 제외
        assertTrue(myUserId !in feeds.map { it.userId })
        assertTrue(UserId(blocked.id.value) !in feeds.map { it.userId })

        // 순서 및 점수 검증
        // 예상 순위: 1등(친구) -> 2등(남, 유사 키워드) -> 3등(남, 다른 키워드)
        val firstFeed = feeds[0]
        val secondFeed = feeds[1]
        val thirdFeed = feeds[2]
        // 1등(친구) 친구 검증
        assertEquals(firstFeed.userId.value, friend.id.value)
        assertTrue(firstFeed.score >= 100.0)
        // 2등(남, 유사 키워드) 검증
        assertEquals(secondFeed.userId.value, stranger.id.value)
        assertEquals(secondFeed.keyword.value, similarKeyword.keyword)
        assertTrue(secondFeed.score >= 40.0)
        assertTrue(secondFeed.score >= thirdFeed.score)
        // 3등(남, 다른 키워드) 검증
        assertEquals(thirdFeed.userId.value, stranger.id.value)
        assertEquals(thirdFeed.keyword.value, diffKeyword.keyword)

        // 피드 개수 검증 (나, 차단된 사용자 글 제외하고 계산)
        assertEquals(3, feeds.size)
    }

    @Test
    fun `피드 페이징 처리가 정상적으로 동작한다`() = runTest {
        // given: 데이터 5개 생성 (점수가 명확히 구분되도록 유사도 조절)
        val me = createUser("me")
        val baseVector = TestVectorFixture.unitVector(1.0f)
        val myKeyword = createKeyword("my", baseVector.toPgVectorString(), me)
        createUserKeyword(me, myKeyword, "내 글")

        // 타인 10명 생성 (유사도를 1.0, 0.9, 0.8 순으로 생성하여 점수 차등화)
        val users = (1..10).map { createUser("other$it") }
        users.forEachIndexed { index, user ->
            val sim = 1.0f - (index * 0.1f)
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

        // then: 1페이지 검증
        assertEquals(2 + 1, firstPage.size)
        val cursorPage = firstPage[2 - 1]

        // when: 2페이지 조회 (1페이지의 마지막 데이터를 커서로 사용)
        val secondPage = repository.getFeeds(
            userId = myUserId,
            cursorScore = cursorPage.score,
            cursorCreatedAt = cursorPage.createdAt,
            cursorUkId = cursorPage.userKeywordId,
            limit = 2,
        )

        // then: 2페이지 검증
        println("========================= 2페이지 결과 =========================")
        secondPage.forEach {
            println("ID: ${it.userKeywordId.value}, 점수: ${it.score}, 작성자: ${it.userName}")
        }

        // 2페이지의 첫 번째 데이터는 1페이지의 마지막 데이터보다 점수가 낮거나(같다면 ID가 작아야 함)
        assertTrue(secondPage[0].score <= cursorPage.score)
        // 1페이지에서 나온 데이터가 2페이지에 중복으로 포함되면 안 됨
        val firstPageIds = firstPage.map { it.userKeywordId.value }
        secondPage.forEach {
            assertTrue(it.userKeywordId.value !in firstPageIds, "중복 데이터 발견: ${it.userKeywordId.value}")
        }

        // when: 마지막 페이지 검증 (절대 조회되지 않는 커서로 조회)
        val emptyPage = repository.getFeeds(
            userId = myUserId,
            cursorScore = -1.0,
            cursorCreatedAt = 0L,
            cursorUkId = UserKeywordId(1000L),
            limit = 2,
        )

        // then
        assertTrue(emptyPage.isEmpty(), "데이터가 없어야 하는데 결과가 존재합니다.")
    }

    // ------------------------------ Helper Functions ------------------------------
    private suspend fun createUser(uniqueValue: String): UserEntity = TestDatabaseFactory.dbQuery {
        UserEntity.new {
            this.provider = SocialLoginProvider.GOOGLE
            this.providerId = "pid$uniqueValue"
            this.displayId = "did$uniqueValue"
            this.name = "name$uniqueValue"
            this.introduce = "introduce$uniqueValue"
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
    ): UserKeywordEntity = TestDatabaseFactory.dbQuery {
        UserKeywordEntity.new {
            this.userId = user.id
            this.keywordId = keyword.id
            this.description = description
        }
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
