package com.turnin.domain.friend.infrastructure.repository

import com.turnin.common.db.DatabaseException
import com.turnin.common.db.schema.BlockReasons
import com.turnin.common.db.schema.Blocks
import com.turnin.common.db.schema.FriendEntity
import com.turnin.common.db.schema.Friends
import com.turnin.common.db.schema.UserEntity
import com.turnin.common.db.schema.UserFcmTokens
import com.turnin.common.db.schema.Users
import com.turnin.common.model.FriendRequestStatus
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.id.UserId
import com.turnin.common.util.toOffsetDateTime
import com.turnin.domain.friend.domain.model.FriendFcmContext
import com.turnin.domain.friend.infrastructure.mapper.FriendMapper.toDomain
import com.turnin.util.db.TestDatabaseFactory
import com.turnin.util.db.setUserInactiveForTest
import com.turnin.util.testPagination
import java.time.Instant
import java.time.OffsetDateTime
import kotlin.collections.isNotEmpty
import kotlin.collections.map
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class FriendRepositoryImplTest {
    private val repository = FriendRepositoryImpl()

    @BeforeTest
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @AfterTest
    fun teardown() {
        TestDatabaseFactory.cleanUp()
    }

    // ------------------------------ getFriendsPagination ------------------------------

    @Test
    fun `친구 목록 페이지네이션 조회 성공 테스트`() = runTest {
        // given: 테스트 기준 사용자 제외 100명의 사용자 생성 후 친구 관계로 세팅
        val totalSize = 86
        val pageSize = 13

        val userId = insertUserAndReturnId("User 1")
        repeat(totalSize) {
            val testUserId = insertUserAndReturnId("test$it")
            repository.createFriend(userId, testUserId)
            repository.updateFriendRequestStatus(testUserId, userId, FriendRequestStatus.ACCEPTED)
        }

        // when, then
        testPagination(
            totalSize = totalSize,
            pageSize = pageSize,
            fetcher = { offset, limit ->
                val friendsPagingData = repository.getFriendsPagination(userId, offset, limit)
                friendsPagingData.friends
            },
        )
    }

    @Test
    fun `친구 목록 조회 성공 테스트`() = runTest {
        // given: 사용자1이 사용자2에게 친구 요청을 보내고 사용자2가 요청을 수락한 상태
        val userId1 = insertUserAndReturnId("a")
        val userId2 = insertUserAndReturnId("b")
        repository.createFriend(userId1, userId2)
        repository.updateFriendRequestStatus(userId2, userId1, FriendRequestStatus.ACCEPTED)

        // when
        val user1Friends = repository.getFriendsPagination(userId1, 0, 10)
        val user2Friends = repository.getFriendsPagination(userId2, 0, 10)

        // then: 사용자1, 사용자2가 서로 친구 사이이기 때문에 두 사용자 모두 친구 수는 1이다.
        assertEquals(1, user1Friends.friends.size)
        assertTrue(user1Friends.friends.first().receiverId == userId2)
        assertEquals(1, user2Friends.friends.size)
        assertTrue(user2Friends.friends.first().requesterId == userId1)
    }

    @Test
    fun `친구 목록 조회 성공 테스트 - 친구가 없다면 빈 리스트를 반환한다`() = runTest {
        // given
        val userId = insertUserAndReturnId("a")

        // when
        val friends = repository.getFriendsPagination(userId, 0, 10)

        // then
        assertTrue(friends.friends.isEmpty())
    }

    // ------------------------------ getIncomingRequests ------------------------------

    @Test
    fun `받은 친구 요청 목록 페이지네이션 조회 성공 테스트`() = runTest {
        // given
        val totalSize = 33
        val pageSize = 10
        val userId = insertUserAndReturnId("user1")
        repeat(totalSize) {
            val testUserId = insertUserAndReturnId("test$it")
            repository.createFriend(requesterId = testUserId, receiverId = userId)
        }

        // when
        testPagination(
            totalSize = totalSize,
            pageSize = pageSize,
            fetcher = { offset, limit ->
                val pagingData = repository.getIncomingRequests(userId, offset, limit)
                pagingData.requests
            },
        )
    }

    @Test
    fun `받은 친구 요청 목록에서 요청을 수락하면 목록에서 제외된다`() = runTest {
        // given
        val userId = insertUserAndReturnId("user1")
        val testUserId = insertUserAndReturnId("test1")
        repository.createFriend(requesterId = testUserId, receiverId = userId)

        // when: 페이징 조회
        // then: 요청 목록에는 1명의 사용자가 조회된다.
        val pagingData = repository.getIncomingRequests(userId, 0, 10)
        assertEquals(1, pagingData.requests.size)

        // when: 친구 요청을 수락한다.
        repository.updateFriendRequestStatus(
            userId,
            testUserId,
            FriendRequestStatus.ACCEPTED,
        )

        // then: 친구가 된 사용자는 요청 목록에서 제외된다.
        val pagingData2 = repository.getIncomingRequests(userId, 0, 10)
        assertTrue(pagingData2.requests.isEmpty())
    }

    @Test
    fun `받은 요청이 없다면 빈 리스트를 반환한다`() = runTest {
        // given
        val userId = insertUserAndReturnId("user1")

        // when
        val pagingData = repository.getIncomingRequests(userId, 0, 10)

        // then
        assertTrue(pagingData.requests.isEmpty())
    }

    // ------------------------------ findByIds ------------------------------

    @Test
    fun `requesterId와 receiverId로 친구 데이터 조회 성공 테스트`() = runTest {
        // given
        val userId1 = insertUserAndReturnId("a")
        val userId2 = insertUserAndReturnId("b")
        repository.createFriend(userId1, userId2)
        val expectedFriend = repository.findByIds(userId1, userId2)

        // when
        val actualFriend = repository.findByIds(userId1, userId2)

        // then
        assertEquals(expectedFriend, actualFriend)
    }

    @Test
    fun `requesterId와 receiverId로 친구 데이터 조회 성공 테스트 - 데이터가 없다면 null을 반환한다`() = runTest {
        // given: 그냥 사용자만 생성해놓고 두 사용자는 아무 관계가 아니다.
        val userId1 = insertUserAndReturnId("a")
        val userId2 = insertUserAndReturnId("b")

        // when
        val friend = repository.findByIds(userId1, userId2)

        // then
        assertNull(friend)
    }

    // ------------------------------ createFriend ------------------------------

    @Test
    fun `친구 요청 성공 테스트`() = runTest {
        // given
        val userId1 = insertUserAndReturnId("a")
        val userId2 = insertUserAndReturnId("b")

        // when: user1이 user2에게 친구 요청
        repository.createFriend(userId1, userId2)
        val friend = repository.findByIds(userId1, userId2)!!
        val nowInSeconds = Instant.now().epochSecond

        // then
        assertEquals(friend.requesterId, userId1)
        assertEquals(friend.receiverId, userId2)
        assertEquals(friend.requestStatus, FriendRequestStatus.PENDING)
        assertTrue(friend.createdAt in (nowInSeconds - 1)..(nowInSeconds + 1))
    }

    @Test
    fun `친구 요청 실패 테스트 - 존재하지 않는 사용자의 ID 사용 시 DB 예외가 발생한다`() = runTest {
        assertThrows<DatabaseException> {
            repository.createFriend(UserId(1L), UserId(2L))
        }
    }

    @Test
    fun `친구 요청 실패 테스트 - 이미 존재하는 요청이 있다면 중복 데이터 예외가 발생한다`() = runTest {
        val userId1 = insertUserAndReturnId("a")
        val userId2 = insertUserAndReturnId("b")
        // 첫번째 요청
        repository.createFriend(userId1, userId2)

        assertThrows<DatabaseException.DuplicatedDataException> {
            // 두번째 요청 - 예외 발생
            repository.createFriend(userId1, userId2)
        }
    }

    // ------------------------------ updateFriendRequestStatus ------------------------------

    @Test
    fun `친구 상태 수정 성공 테스트`() = runTest {
        // given
        val userId1 = insertUserAndReturnId("a")
        val userId2 = insertUserAndReturnId("b")
        repository.createFriend(userId1, userId2) // 반환값 제거
        val expectedFriend = repository.findByIds(userId1, userId2)!! // 별도 조회로 변경
        val originalUpdatedAt = TestDatabaseFactory.dbQuery {
            FriendEntity
                .findById(expectedFriend.id.value)
                ?.updatedAt
        }

        // when, then: PENDING(초기 값), ACCEPTED 순서대로 검증
        // 1. PENDING
        val friend = TestDatabaseFactory.dbQuery {
            Friends
                .selectAll()
                .where((Friends.id eq expectedFriend.id.value))
                .singleOrNull()
        }
        assertNotNull(friend)
        assertEquals(FriendRequestStatus.PENDING, friend[Friends.status])

        // 2. ACCEPTED
        repository.updateFriendRequestStatus(
            updaterId = userId2,
            requesterId = userId1,
            requestStatus = FriendRequestStatus.ACCEPTED,
        )
        val friend2 = TestDatabaseFactory.dbQuery {
            Friends
                .selectAll()
                .where((Friends.id eq expectedFriend.id.value))
                .singleOrNull()
        }

        assertNotNull(friend2)
        assertEquals(FriendRequestStatus.ACCEPTED, friend2[Friends.status])
        assertNotNull(friend2.toDomain().respondedAt)

        // updatedAt은 UPDATE 후 갱신됐어야 함
        val updatedAt = TestDatabaseFactory.dbQuery { FriendEntity.findById(friend2[Friends.id])?.updatedAt }
        assertTrue(updatedAt!!.isAfter(originalUpdatedAt))
    }

    @Test
    fun `친구 상태 수정 실패 테스트 - 존재하지 않는 사용자 ID로 상태 수정 시도 시 false를 반환한다`() = runTest {
        // given
        val userId1 = insertUserAndReturnId("a")

        // when: 수락하는 사람 -> 존재하지 않는 원래 요청자
        val result = repository.updateFriendRequestStatus(
            updaterId = userId1,
            requesterId = UserId(10L),
            requestStatus = FriendRequestStatus.ACCEPTED,
        )

        // then
        assertFalse(result)
    }

    // ------------------------------ deleteFriend ------------------------------

    @Test
    fun `친구 삭제 성공 테스트`() = runTest {
        // given
        val userId1 = insertUserAndReturnId("a")
        val userId2 = insertUserAndReturnId("b")
        repository.createFriend(userId1, userId2)

        // when
        val result = repository.deleteFriend(userId1, userId2)
        val user1Friends = repository.getFriendsPagination(userId1, 0, 10)
        val user2Friends = repository.getFriendsPagination(userId2, 0, 10)

        // then: 사용자1, 사용자2 모두 서로에 대한 친구 관계 데이터가 존재하지 않는다.
        assertTrue(result)
        assertTrue(user1Friends.friends.isEmpty())
        assertTrue(user2Friends.friends.isEmpty())
    }

    @Test
    fun `친구 삭제 실패 테스트 - 존재하지 않는 사용자 ID로 삭제 시도 시 false를 반환한다`() = runTest {
        // given
        val userId1 = insertUserAndReturnId("a")

        // when
        val result = repository.deleteFriend(userId1, UserId(10L))

        // then
        assertFalse(result)
    }

    // ------------------------------ countFriends ------------------------------

    @Test
    fun `친구 수 조회 성공 테스트`() = runTest {
        // given: 사용자 1이 사용자2에게 친구 요청 후 수락
        val userId1 = insertUserAndReturnId("a")
        val userId2 = insertUserAndReturnId("b")
        repository.createFriend(userId1, userId2)
        repository.updateFriendRequestStatus(userId2, userId1, FriendRequestStatus.ACCEPTED)

        // when
        val count = repository.countFriends(userId1)

        // then
        assertEquals(1, count)
    }

    // ------------------------------ getUserInfos ------------------------------

    @Test
    fun `getUserInfos 성공 테스트`() = runTest {
        // given: 10명의 테스트 사용자를 생성
        val savedUserId = mutableListOf<UserId>()
        val userTotalCount = 10
        repeat(userTotalCount) {
            val userId = insertUserAndReturnId("${it + 1L}")
            savedUserId.add(userId)
        }

        // when
        val users = repository.getUserInfos(savedUserId)

        // then
        assertTrue(users.isNotEmpty())
        assertEquals(userTotalCount, users.size)
        assertEquals(savedUserId, users.map { it.userId })
    }

    @Test
    fun `getUserInfos 성공 테스트 - 비활성화 사용자는 조회되지 않는다`() = runTest {
        // given: 1명의 테스트 사용자를 생성, 해당 사용자 비활성화
        val userId = insertUserAndReturnId("1")
        setUserInactiveForTest(userId)

        // when: 비활성화 사용자 조회
        val users = repository.getUserInfos(listOf(userId))

        // then: 사용자가 조회되지 않는다.
        assertEquals(0, users.size)
    }

    // ------------------------------ deleteAll ------------------------------

    @Test
    fun `deleteAll 성공 테스트`() = runTest {
        // given: 사용자 생성 후 친구 관계 설정
        val userId = insertUserAndReturnId("me")
        val friendUserId1 = insertUserAndReturnId("1")
        val friendUserId2 = insertUserAndReturnId("2")

        val friend1 = createFriendForTest(userId, friendUserId1, FriendRequestStatus.ACCEPTED)
        val friend2 = createFriendForTest(userId, friendUserId2, FriendRequestStatus.PENDING)

        assertNotNull(findByIdForTest(friend1.id.value))
        assertNotNull(findByIdForTest(friend2.id.value))

        // when: 모든 친구 관계 삭제
        repository.deleteAll(userId)

        // then: 친구가 삭제됐는지 검증
        assertNull(findByIdForTest(friend1.id.value))
        assertNull(findByIdForTest(friend2.id.value))
        val friends = TestDatabaseFactory.dbQuery {
            Friends
                .selectAll()
                .where {
                    (Friends.requesterId eq userId.value) or
                        (Friends.receiverId eq userId.value)
                }.map { it[Friends.id].value }
        }
        assertEquals(0, friends.size)
    }

    // ------------------------------ getFriendRequestContext ------------------------------
    @Test
    fun `getFriendRequestContext 성공 테스트 - 요청자와 수신자 정보를 반환한다`() = runTest {
        // given
        val requesterId = insertUserAndReturnId("requester")
        val receiverId = insertUserAndReturnId("receiver")

        // when
        val context = repository.getFriendRequestContext(requesterId, receiverId)

        // then
        assertNotNull(context)
        assertEquals(requesterId, context.requesterInfo.userId)
        assertEquals(receiverId, context.receiverInfo.userId)
        assertFalse(context.isBlocked)
        assertNull(context.existingRelation) // 아무 관계도 없는 상태
    }

    @Test
    fun `getFriendRequestContext 성공 테스트 - 요청자가 존재하지 않으면 null을 반환한다`() = runTest {
        // given
        val receiverId = insertUserAndReturnId("receiver")

        // when
        val context = repository.getFriendRequestContext(UserId(999L), receiverId)

        // then
        assertNull(context)
    }

    @Test
    fun `getFriendRequestContext 성공 테스트 - 수신자가 존재하지 않으면 null을 반환한다`() = runTest {
        // given
        val requesterId = insertUserAndReturnId("requester")

        // when
        val context = repository.getFriendRequestContext(requesterId, UserId(999L))

        // then
        assertNull(context)
    }

    @Test
    fun `getFriendRequestContext 성공 테스트 - 비활성화된 수신자는 null을 반환한다`() = runTest {
        // given
        val requesterId = insertUserAndReturnId("requester")
        val receiverId = insertUserAndReturnId("receiver")
        setUserInactiveForTest(receiverId)

        // when
        val context = repository.getFriendRequestContext(requesterId, receiverId)

        // then
        assertNull(context)
    }

    @Test
    fun `getFriendRequestContext 성공 테스트 - 요청자가 수신자를 차단한 경우 isBlocked가 true이다`() = runTest {
        // given
        val requesterId = insertUserAndReturnId("requester")
        val receiverId = insertUserAndReturnId("receiver")
        createBlockForTest(blockerId = requesterId, blockedId = receiverId)

        // when
        val context = repository.getFriendRequestContext(requesterId, receiverId)

        // then
        assertNotNull(context)
        assertTrue(context.isBlocked)
        assertNull(context.existingRelation)
    }

    @Test
    fun `getFriendRequestContext 성공 테스트 - 수신자가 요청자를 차단한 경우 isBlocked가 true이다`() = runTest {
        // given
        val requesterId = insertUserAndReturnId("requester")
        val receiverId = insertUserAndReturnId("receiver")
        createBlockForTest(blockerId = receiverId, blockedId = requesterId)

        // when
        val context = repository.getFriendRequestContext(requesterId, receiverId)

        // then
        assertNotNull(context)
        assertTrue(context.isBlocked)
        assertNull(context.existingRelation)
    }

    @Test
    fun `getFriendRequestContext 성공 테스트 - 정방향 PENDING 요청이 있는 경우 existingRelation을 반환한다`() = runTest {
        // given: requesterId가 receiverId에게 친구 요청을 보낸 상태
        val requesterId = insertUserAndReturnId("requester")
        val receiverId = insertUserAndReturnId("receiver")
        repository.createFriend(requesterId, receiverId)

        // when
        val context = repository.getFriendRequestContext(requesterId, receiverId)

        // then
        assertNotNull(context)
        assertNotNull(context.existingRelation)
        assertEquals(FriendRequestStatus.PENDING, context.existingRelation.status)
        assertFalse(context.existingRelation.isReverse) // 정방향
    }

    @Test
    fun `getFriendRequestContext 성공 테스트 - 역방향 PENDING 요청이 있는 경우 existingRelation을 반환한다`() = runTest {
        // given: receiverId가 requesterId에게 친구 요청을 보낸 상태 (역방향)
        val requesterId = insertUserAndReturnId("requester")
        val receiverId = insertUserAndReturnId("receiver")
        repository.createFriend(receiverId, requesterId)

        // when
        val context = repository.getFriendRequestContext(requesterId, receiverId)

        // then
        assertNotNull(context)
        assertNotNull(context.existingRelation)
        assertEquals(FriendRequestStatus.PENDING, context.existingRelation.status)
        assertTrue(context.existingRelation.isReverse) // 역방향
    }

    @Test
    fun `getFriendRequestContext 성공 테스트 - 이미 친구 상태인 경우 ACCEPTED existingRelation을 반환한다`() = runTest {
        // given
        val requesterId = insertUserAndReturnId("requester")
        val receiverId = insertUserAndReturnId("receiver")
        repository.createFriend(requesterId, receiverId)
        repository.updateFriendRequestStatus(
            updaterId = receiverId,
            requesterId = requesterId,
            requestStatus = FriendRequestStatus.ACCEPTED,
        )

        // when
        val context = repository.getFriendRequestContext(requesterId, receiverId)

        // then
        assertNotNull(context)
        assertNotNull(context.existingRelation)
        assertEquals(FriendRequestStatus.ACCEPTED, context.existingRelation.status)
    }

    // ------------------------------ getFriendFcmContext ------------------------------

    @Test
    fun `getFriendFcmContext 성공 테스트 - 수락된 친구들의 활성화된 토큰과 내 이름을 반환한다`() = runTest {
        // given
        val myName = "나의이름"
        val myId = insertUserAndReturnId(myName)
        val friendId1 = insertUserAndReturnId("friend1")
        val friendId2 = insertUserAndReturnId("friend2")

        // 친구 관계 설정 (수락 상태)
        repository.createFriend(myId, friendId1)
        repository.updateFriendRequestStatus(friendId1, myId, FriendRequestStatus.ACCEPTED)
        repository.createFriend(friendId2, myId) // 상대방이 신청한 경우
        repository.updateFriendRequestStatus(myId, friendId2, FriendRequestStatus.ACCEPTED)

        // 토큰 설정
        val now = Instant.now().toOffsetDateTime()
        insertFcmTokenForTest(friendId1, "token1", isActive = true, updatedAt = now)
        insertFcmTokenForTest(friendId2, "token3_old", isActive = true, updatedAt = now.minusSeconds(600))
        insertFcmTokenForTest(friendId2, "token2", isActive = true, updatedAt = now)

        // when
        val context = repository.getFriendFcmContext(myId)

        // then
        assertEquals(myName, context.senderName)
        assertEquals(2, context.friendTokens.size)
        assertTrue(context.friendTokens.containsAll(listOf("token1", "token2")))
    }

    @Test
    fun `getFriendFcmContext 성공 테스트 - 친구가 없거나 토큰이 없으면 빈 목록을 반환한다`() = runTest {
        // given
        val myId = insertUserAndReturnId("me")

        // when
        val context = repository.getFriendFcmContext(myId)

        // then
        assertTrue(context.friendTokens.isEmpty())
        assertEquals("", context.senderName) // JOIN 결과가 없어 이름도 빈값
    }

    @Test
    fun `getFriendFcmContext 성공 테스트 - 비활성화된 토큰이나 수락되지 않은 친구는 제외한다`() = runTest {
        // given
        val myId = insertUserAndReturnId("me")
        val friendId1 = insertUserAndReturnId("friend1")
        val friendId2 = insertUserAndReturnId("friend2")

        // friend1: 수락됨, 하지만 토큰 비활성
        repository.createFriend(myId, friendId1)
        repository.updateFriendRequestStatus(friendId1, myId, FriendRequestStatus.ACCEPTED)
        insertFcmTokenForTest(friendId1, "inactive_token", isActive = false)

        // friend2: 대기중(PENDING), 토큰 활성
        repository.createFriend(myId, friendId2)
        insertFcmTokenForTest(friendId2, "pending_friend_token", isActive = true)

        // when
        val context = repository.getFriendFcmContext(myId)

        // then
        assertTrue(context.friendTokens.isEmpty())
    }

    @Test
    fun `getFriendFcmContext 성공 테스트 - 최대 제한 수만큼만 토큰을 가져온다`() = runTest {
        // given
        val myId = insertUserAndReturnId("me")
        val limit = FriendFcmContext.MAX_NOTIFICATION_RECIPIENTS

        // 제한 수보다 많은 친구 생성 (예: limit + 10)
        repeat(limit + 10) {
            val friendId = insertUserAndReturnId("friend$it")
            repository.createFriend(myId, friendId)
            repository.updateFriendRequestStatus(friendId, myId, FriendRequestStatus.ACCEPTED)
            insertFcmTokenForTest(friendId, "token$it", isActive = true)
        }

        // when
        val context = repository.getFriendFcmContext(myId)

        // then
        assertEquals(limit, context.friendTokens.size)
    }

    // ------------------------------ 유틸 ------------------------------

    private suspend fun insertUserAndReturnId(uniqueValue: String): UserId = TestDatabaseFactory.dbQuery {
        val savedUser = UserEntity.new {
            this.role = Role.USER
            this.provider = SocialLoginProvider.GOOGLE
            this.providerId = "pid$uniqueValue"
            this.displayId = "did$uniqueValue"
            this.name = uniqueValue
            this.profileImageUrl = null
            this.introduce = "hello$uniqueValue"
            this.isActive = true
            this.lastLoginAt = Instant.now()
        }

        UserId(savedUser.id.value)
    }

    private suspend fun createFriendForTest(
        userId1: UserId,
        userId2: UserId,
        status: FriendRequestStatus,
    ): FriendEntity =
        TestDatabaseFactory.dbQuery {
            FriendEntity.new {
                this.requesterId = EntityID(userId1.value, Users)
                this.receiverId = EntityID(userId2.value, Users)
                this.status = status
                this.respondedAt = null
            }
        }

    private suspend fun findByIdForTest(friendId: Long): FriendEntity? = TestDatabaseFactory.dbQuery {
        FriendEntity.findById(friendId)
    }

    private suspend fun createBlockForTest(
        blockerId: UserId,
        blockedId: UserId,
    ): Unit = TestDatabaseFactory.dbQuery {
        Blocks.insert {
            it[Blocks.blockerId] = EntityID(blockerId.value, Users)
            it[Blocks.blockedId] = EntityID(blockedId.value, Users)
            it[Blocks.reasonId] = EntityID(1L, BlockReasons) // 기존 initData에서 생성된 차단 사유
            it[Blocks.customReason] = null
        }
    }

    private suspend fun insertFcmTokenForTest(
        userId: UserId,
        token: String,
        isActive: Boolean,
        updatedAt: OffsetDateTime = Instant.now().toOffsetDateTime(),
    ) = TestDatabaseFactory.dbQuery {
        UserFcmTokens.insert {
            it[UserFcmTokens.userId] = EntityID(userId.value, Users)
            it[UserFcmTokens.token] = token
            it[UserFcmTokens.isActive] = isActive
            it[UserFcmTokens.updatedAt] = updatedAt
        }
    }
}
