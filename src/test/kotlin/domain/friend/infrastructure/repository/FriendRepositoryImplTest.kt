package com.peekr.domain.friend.infrastructure.repository

import com.peekr.common.db.DatabaseException
import com.peekr.common.db.schema.BlockReasons
import com.peekr.common.db.schema.Blocks
import com.peekr.common.db.schema.FriendEntity
import com.peekr.common.db.schema.Friends
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.db.schema.Users
import com.peekr.common.model.FriendRequestStatus
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.id.UserId
import com.peekr.domain.friend.infrastructure.mapper.FriendMapper.toDomain
import com.peekr.util.db.TestDatabaseFactory
import com.peekr.util.db.setUserInactiveForTest
import com.peekr.util.testPagination
import java.time.Instant
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
            testUserId,
            userId,
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
        val expectedFriend = repository.createFriend(userId1, userId2)

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
        val friend = repository.createFriend(userId1, userId2)
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
        val expectedFriend = repository.createFriend(userId1, userId2)
        val originalUpdatedAt = TestDatabaseFactory.dbQuery {
            FriendEntity
                .findById(
                    expectedFriend.id.value,
                )?.updatedAt
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
        repository.updateFriendRequestStatus(userId2, userId1, FriendRequestStatus.ACCEPTED)
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

        // when
        val result = repository.updateFriendRequestStatus(userId1, UserId(10L), FriendRequestStatus.ACCEPTED)

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
    }

    private suspend fun insertUserAndReturnId(uniqueValue: String): UserId = TestDatabaseFactory.dbQuery {
        val savedUser = UserEntity.new {
            this.role = Role.USER
            this.provider = SocialLoginProvider.GOOGLE
            this.providerId = "pid$uniqueValue"
            this.displayId = "did$uniqueValue"
            this.name = "honggd"
            this.profileImageUrl = null
            this.introduce = "hello"
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
}
