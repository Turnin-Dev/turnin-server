package com.peekr.domain.friend.application.usecase

import com.peekr.common.model.FriendRequestStatus
import com.peekr.common.model.UserName
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId
import com.peekr.domain.friend.domain.model.FriendRequestContext
import com.peekr.domain.friend.domain.model.UserInfo
import com.peekr.domain.friend.domain.provider.NotificationProvider
import com.peekr.domain.friend.domain.repository.FriendRepository
import com.peekr.domain.friend.exception.FriendException
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertThrows

class UpdateFriendRequestStatusUseCaseTest {
    private val friendRepository: FriendRepository = mockk()
    private val notificationProvider: NotificationProvider = mockk()
    private val usecase = UpdateFriendRequestStatusUseCase(friendRepository, notificationProvider)

    @BeforeTest
    fun setUp() {
        coEvery {
            friendRepository.getFriendRequestContext(TestUserId1, TestUserId2)
        } returns TestFriendRequestContext

        coEvery {
            friendRepository.updateFriendRequestStatus(TestUserId1, TestUserId2, any())
        } returns true

        coEvery {
            notificationProvider.sendNotification(any())
        } just Runs
    }

    @Test
    fun `친구 상태 수정 성공 테스트`() = runTest {
        // when
        val result = usecase(TestUserId1.value, TestUserId2.value, FriendRequestStatus.ACCEPTED)

        // then
        assertTrue(result)
    }

    @Test
    fun `친구 요청 수락 시 수신자에게 알림을 전송한다`() = runTest {
        // when
        usecase(TestUserId1.value, TestUserId2.value, FriendRequestStatus.ACCEPTED)

        // then
        coVerify(exactly = 1) { notificationProvider.sendNotification(any()) }
    }

    @Test
    fun `친구 요청 거절 시 알림을 전송하지 않는다`() = runTest {
        // when
        usecase(TestUserId1.value, TestUserId2.value, FriendRequestStatus.REJECTED)

        // then
        coVerify(exactly = 0) { notificationProvider.sendNotification(any()) }
    }

    @Test
    fun `알림 전송 실패 시에도 상태 수정은 성공으로 처리한다`() = runTest {
        // given
        coEvery {
            notificationProvider.sendNotification(any())
        } throws RuntimeException("알림 전송 실패")

        // when
        val result = usecase(TestUserId1.value, TestUserId2.value, FriendRequestStatus.ACCEPTED)

        // then
        assertTrue(result)
    }

    @Test
    fun `요청한 사용자 ID와 요청받은 사용자 ID가 같으면 예외가 발생한다`() = runTest {
        assertThrows<FriendException.SelfRequestException> {
            usecase(1L, 1L, FriendRequestStatus.ACCEPTED)
        }
    }

    @Test
    fun `상태를 수정하려는 사용자가 존재하지 않는 경우 예외가 발생한다`() = runTest {
        // given
        coEvery {
            friendRepository.getFriendRequestContext(TestUserId1, TestUserId2)
        } returns null

        // when, then
        assertThrows<FriendException.UserNotFoundException> {
            usecase(TestUserId1.value, TestUserId2.value, FriendRequestStatus.ACCEPTED)
        }
    }

    @Test
    fun `상태 수정 실패 시 알림을 전송하지 않는다`() = runTest {
        // given
        coEvery {
            friendRepository.updateFriendRequestStatus(TestUserId1, TestUserId2, any())
        } returns false

        // when
        usecase(TestUserId1.value, TestUserId2.value, FriendRequestStatus.ACCEPTED)

        // then
        coVerify(exactly = 0) { notificationProvider.sendNotification(any()) }
    }

    companion object {
        private val TestUserId1 = UserId(1L)
        private val TestUserId2 = UserId(2L)
        private val TestFriendRequestContext = FriendRequestContext(
            requesterInfo = UserInfo(
                userId = TestUserId1,
                displayId = DisplayId("did1"),
                userName = UserName("테스트유저1"),
                profileImageUrl = null,
            ),
            receiverInfo = UserInfo(
                userId = TestUserId2,
                displayId = DisplayId("did2"),
                userName = UserName("테스트유저2"),
                profileImageUrl = null,
            ),
            isBlocked = false,
        )
    }
}
