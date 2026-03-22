package com.peekr.domain.friend.application.usecase

import com.peekr.common.db.DatabaseException
import com.peekr.common.model.FriendRequestStatus
import com.peekr.common.model.UserName
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.FriendId
import com.peekr.common.model.id.UserId
import com.peekr.domain.friend.domain.model.Friend
import com.peekr.domain.friend.domain.model.FriendRequestContext
import com.peekr.domain.friend.domain.model.UserInfo
import com.peekr.domain.friend.domain.provider.NotificationProvider
import com.peekr.domain.friend.domain.repository.FriendRepository
import com.peekr.domain.friend.exception.FriendException
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class AddFriendUseCaseTest {
    private val friendRepository: FriendRepository = mockk()
    private val notificationProvider: NotificationProvider = mockk()
    private val usecase = AddFriendUseCase(friendRepository, notificationProvider)

    @Before
    fun setup() {
        coEvery {
            friendRepository.getFriendRequestContext(TestRequesterId, TestReceiverId)
        } returns TestFriendRequestContext

        coEvery {
            notificationProvider.sendNotification(any())
        } just Runs
    }

    @After
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `친구 추가 성공 테스트`() = runTest {
        // given
        coEvery {
            friendRepository.createFriend(TestRequesterId, TestReceiverId)
        } returns TestFriend

        // when
        val friendDto = usecase(
            requesterId = TestRequesterId.value,
            receiverId = TestReceiverId.value,
        )

        // then
        assertEquals(TestRequesterId.value, friendDto.requesterId)
        assertEquals(TestReceiverId.value, friendDto.receiverId)
        coVerify(exactly = 1) { notificationProvider.sendNotification(any()) }
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
    }

    @Test
    fun `친구 요청한 사용자 ID와 요청 받은 사용자 ID가 같을 때 예외가 발생한다`() = runTest {
        // when, then
        assertThrows<FriendException.SelfRequestException> {
            usecase(1L, 1L)
        }
    }

    @Test
    fun `이미 친구 요청을 했거나 친구 상태인 경우 예외가 발생한다`() = runTest {
        // given
        coEvery {
            friendRepository.createFriend(TestRequesterId, TestReceiverId)
        } throws DatabaseException.DuplicatedDataException(Throwable())

        // when, then
        assertThrows<FriendException.AlreadyFriendRequestException> {
            usecase(
                requesterId = TestRequesterId.value,
                receiverId = TestReceiverId.value,
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
        val friendDto = usecase(
            requesterId = TestRequesterId.value,
            receiverId = TestReceiverId.value,
        )

        // then
        assertEquals(TestRequesterId.value, friendDto.requesterId)
        assertEquals(TestReceiverId.value, friendDto.receiverId)
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
