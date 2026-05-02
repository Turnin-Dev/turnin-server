package com.turnin.domain.friend.application.usecase

import com.turnin.common.model.FriendRequestStatus
import com.turnin.common.model.NotificationType
import com.turnin.common.model.UserName
import com.turnin.common.model.id.DisplayId
import com.turnin.common.model.id.FriendId
import com.turnin.common.model.id.UserId
import com.turnin.domain.friend.domain.model.ExistingRelation
import com.turnin.domain.friend.domain.model.Friend
import com.turnin.domain.friend.domain.model.FriendRequestContext
import com.turnin.domain.friend.domain.model.UserInfo
import com.turnin.domain.friend.domain.provider.NotificationProvider
import com.turnin.domain.friend.domain.repository.FriendRepository
import com.turnin.domain.friend.exception.FriendException
import com.turnin.util.db.TestDatabaseFactory
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalCoroutinesApi::class)
class AddFriendUseCaseTest {
    private val friendRepository: FriendRepository = mockk()
    private val notificationProvider: NotificationProvider = mockk()
    private val applicationScope = TestScope()
    private val usecase = AddFriendUseCase(friendRepository, notificationProvider, applicationScope)

    @Before
    fun setup() {
        TestDatabaseFactory.init()

        coEvery {
            friendRepository.getFriendRequestContext(TestRequesterId, TestReceiverId)
        } returns TestFriendRequestContext

        coEvery {
            notificationProvider.sendNotification(any())
        } just Runs
    }

    @After
    fun teardown() {
        TestDatabaseFactory.cleanUp()

        clearAllMocks()
    }

    @Test
    fun `친구 추가 성공 테스트`() = runTest {
        // given
        coEvery {
            friendRepository.createFriend(TestRequesterId, TestReceiverId)
        } returns TestFriend

        // when
        usecase(
            requesterId = TestRequesterId.value,
            receiverId = TestReceiverId.value,
        )

        // then
        applicationScope.advanceUntilIdle()
        coVerify(exactly = 1) {
            notificationProvider.sendNotification(
                match { it.notiType == NotificationType.FRIEND_REQUEST },
            )
        }
    }

    @Test
    fun `요청 받을 사용자가 존재하지 않을 때 예외가 발생한다`() = runTest {
        // given
        coEvery {
            friendRepository.getFriendRequestContext(TestRequesterId, TestReceiverId)
        } returns null

        // when, then
        assertThrows<FriendException.UserNotFoundException> {
            usecase(
                requesterId = TestRequesterId.value,
                receiverId = TestReceiverId.value,
            )
        }

        // then: 예외 발생 시 알림은 전송되지 않아야 함
        applicationScope.advanceUntilIdle()
        coVerify(exactly = 0) { notificationProvider.sendNotification(any()) }
    }

    @Test
    fun `친구 요청한 사용자 ID와 요청 받은 사용자 ID가 같을 때 예외가 발생한다`() = runTest {
        // when, then
        assertThrows<FriendException.SelfRequestException> {
            usecase(1L, 1L)
        }

        // then: 비즈니스 룰 위반 시 DB 조회 조차 하지 않아야 함
        coVerify(exactly = 0) { friendRepository.getFriendRequestContext(UserId(1L), UserId(1L)) }
    }

    @Test
    fun `이미 친구 상태인 경우 예외가 발생한다`() = runTest {
        // given
        coEvery {
            friendRepository.getFriendRequestContext(TestRequesterId, TestReceiverId)
        } returns TestFriendRequestContext.copy(
            existingRelation = ExistingRelation(
                status = FriendRequestStatus.ACCEPTED,
                isReverse = false,
            ),
        )

        // when, then
        assertThrows<FriendException.AlreadyFriendException> {
            usecase(
                requesterId = TestRequesterId.value,
                receiverId = TestReceiverId.value,
            )
        }

        // then: 예외 발생 시 알림은 전송되지 않아야 함
        applicationScope.advanceUntilIdle()
        coVerify(exactly = 0) { notificationProvider.sendNotification(any()) }
    }

    @Test
    fun `동일 방향 중복 요청인 경우 예외가 발생한다`() = runTest {
        // given
        coEvery {
            friendRepository.getFriendRequestContext(TestRequesterId, TestReceiverId)
        } returns TestFriendRequestContext.copy(
            existingRelation = ExistingRelation(
                status = FriendRequestStatus.PENDING,
                isReverse = false,
            ),
        )

        // when, then
        assertThrows<FriendException.AlreadyFriendRequestException> {
            usecase(
                requesterId = TestRequesterId.value,
                receiverId = TestReceiverId.value,
            )
        }

        // then: 예외 발생 시 알림은 전송되지 않아야 함
        applicationScope.advanceUntilIdle()
        coVerify(exactly = 0) { notificationProvider.sendNotification(any()) }
    }

    @Test
    fun `역방향 요청이 존재하는 경우 자동 수락 처리된다`() = runTest {
        // given: 수신자가 이미 요청자에게 친구 요청을 보낸 상태 (역방향)
        coEvery {
            friendRepository.getFriendRequestContext(TestRequesterId, TestReceiverId)
        } returns TestFriendRequestContext.copy(
            existingRelation = ExistingRelation(
                status = FriendRequestStatus.PENDING,
                isReverse = true,
            ),
        )
        coEvery {
            friendRepository.updateFriendRequestStatus(
                updaterId = TestRequesterId,
                requesterId = TestReceiverId,
                requestStatus = FriendRequestStatus.ACCEPTED,
            )
        } returns true

        // when
        usecase(
            requesterId = TestRequesterId.value,
            receiverId = TestReceiverId.value,
        )

        // then: 자동 수락이므로 createFriend는 호출되지 않아야 함
        coVerify(exactly = 0) { friendRepository.createFriend(TestRequesterId, TestReceiverId) }
        coVerify(exactly = 1) {
            friendRepository.updateFriendRequestStatus(
                updaterId = TestRequesterId,
                requesterId = TestReceiverId,
                requestStatus = FriendRequestStatus.ACCEPTED,
            )
        }
    }

    @Test
    fun `역방향 요청 자동 수락 시 수락 알림이 전송된다`() = runTest {
        // given
        coEvery {
            friendRepository.getFriendRequestContext(TestRequesterId, TestReceiverId)
        } returns TestFriendRequestContext.copy(
            existingRelation = ExistingRelation(
                status = FriendRequestStatus.PENDING,
                isReverse = true,
            ),
        )
        coEvery {
            friendRepository.updateFriendRequestStatus(
                updaterId = TestRequesterId,
                requesterId = TestReceiverId,
                requestStatus = FriendRequestStatus.ACCEPTED,
            )
        } returns true

        // when
        usecase(
            requesterId = TestRequesterId.value,
            receiverId = TestReceiverId.value,
        )

        // then: 요청 알림이 아닌 수락 알림이 전송돼야 함
        applicationScope.advanceUntilIdle()
        coVerify(exactly = 1) {
            notificationProvider.sendNotification(
                match { it.notiType == NotificationType.FRIEND_ACCEPT },
            )
        }
    }

    @Test
    fun `친구 요청 하려는 사용자와 차단 관계에 있는 경우 예외가 발생한다`() = runTest {
        // given
        coEvery {
            friendRepository.getFriendRequestContext(TestRequesterId, TestReceiverId)
        } returns TestFriendRequestContext.copy(isBlocked = true)

        // when, then
        assertThrows<FriendException.UserNotFoundException> {
            usecase(
                requesterId = TestRequesterId.value,
                receiverId = TestReceiverId.value,
            )
        }
    }

    @Test
    fun `알림 전송 실패해도 친구 요청은 성공한다`() = runTest {
        // given
        coEvery {
            friendRepository.createFriend(TestRequesterId, TestReceiverId)
        } returns TestFriend
        coEvery {
            notificationProvider.sendNotification(any())
        } throws Exception("알림 전송 실패")

        // when
        usecase(
            requesterId = TestRequesterId.value,
            receiverId = TestReceiverId.value,
        )

        // then: 알림 전송 시도는 이루어졌음을 확인
        applicationScope.advanceUntilIdle()
        coVerify(exactly = 1) { notificationProvider.sendNotification(any()) }
    }

    @Test
    fun `DB 생성 로직 실패 시 알림 전송은 호출되지 않아야 한다`() = runTest {
        // given: 조회는 성공하지만 생성(트랜잭션 핵심부)에서 실패하는 상황
        coEvery {
            friendRepository.createFriend(TestRequesterId, TestReceiverId)
        } throws RuntimeException("DB 저장 실패")

        // when
        assertThrows<RuntimeException> {
            usecase(
                requesterId = TestRequesterId.value,
                receiverId = TestReceiverId.value,
            )
        }

        // then: 트랜잭션이 실패했으므로 알림은 전송되지 않아야 함
        applicationScope.advanceUntilIdle()
        coVerify(exactly = 0) { notificationProvider.sendNotification(any()) }
    }

    companion object {
        private val TestRequesterId = UserId(1L)
        private val TestReceiverId = UserId(2L)
        private val TestFriendRequestContext = FriendRequestContext(
            requesterInfo = UserInfo(
                userId = TestRequesterId,
                displayId = DisplayId("did1"),
                userName = UserName("requester"),
                profileImageUrl = null,
            ),
            receiverInfo = UserInfo(
                userId = TestReceiverId,
                displayId = DisplayId("did2"),
                userName = UserName("receiver"),
                profileImageUrl = null,
            ),
            isBlocked = false,
            existingRelation = null,
        )
        private val TestFriend = Friend(
            id = FriendId(1L),
            requesterId = TestRequesterId,
            receiverId = TestReceiverId,
            requestStatus = FriendRequestStatus.PENDING,
            respondedAt = null,
            createdAt = 1000,
            updatedAt = 1000,
        )
    }
}
