package com.peekr.domain.friend.infrastructure.repository

import com.peekr.common.db.DatabaseException
import com.peekr.common.db.schema.Friends
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.model.FriendRequestStatus
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.id.UserId
import com.peekr.common.util.pagination.PaginationParams
import com.peekr.util.TestDatabaseFactory
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
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

    @Test
    fun `친구 목록 조회 성공 테스트`() = runTest {
        // given: 사용자1이 사용자2에게 친구 요청을 보내고 사용자2가 요청을 수락한 상태
        val userId1 = insertUserAndReturnId("a")
        val userId2 = insertUserAndReturnId("b")
        repository.createFriend(userId1, userId2)
        repository.updateFriendRequestStatus(userId2, userId1, FriendRequestStatus.ACCEPTED)

        // when
        val user1Friends = repository.getFriendsPagination(userId1, PaginationParams(0, 10))
        val user2Friends = repository.getFriendsPagination(userId2, PaginationParams(0, 10))

        // then: 사용자1, 사용자2가 서로 친구 사이이기 때문에 두 사용자 모두 친구 수는 1이다.
        assertEquals(1, user1Friends.size)
        assertTrue(user1Friends.first().receiverId == userId2)
        assertEquals(1, user2Friends.size)
        assertTrue(user2Friends.first().requesterId == userId1)
    }

    @Test
    fun `친구 목록 조회 성공 테스트 - 친구가 없다면 빈 리스트를 반환한다`() = runTest {
        // given
        val userId = insertUserAndReturnId("a")

        // when
        val friends = repository.getFriendsPagination(userId, PaginationParams(1, 10))

        // then
        assertTrue(friends.isEmpty())
    }

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

    @Test
    fun `친구 상태 수정 성공 테스트`() = runTest {
        // given
        val userId1 = insertUserAndReturnId("a")
        val userId2 = insertUserAndReturnId("b")
        val expectedFriend = repository.createFriend(userId1, userId2)

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

    @Test
    fun `친구 삭제 성공 테스트`() = runTest {
        // given
        val userId1 = insertUserAndReturnId("a")
        val userId2 = insertUserAndReturnId("b")
        repository.createFriend(userId1, userId2)

        // when
        val result = repository.deleteFriend(userId1, userId2)
        val user1Friends = repository.getFriendsPagination(userId1, PaginationParams(1, 10))
        val user2Friends = repository.getFriendsPagination(userId2, PaginationParams(1, 10))

        // then: 사용자1, 사용자2 모두 서로에 대한 친구 관계 데이터가 존재하지 않는다.
        assertTrue(result)
        assertTrue(user1Friends.isEmpty())
        assertTrue(user2Friends.isEmpty())
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
}
