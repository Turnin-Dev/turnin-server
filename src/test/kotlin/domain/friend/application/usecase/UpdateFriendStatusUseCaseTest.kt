package com.turnin.domain.friend.application.usecase

import com.turnin.common.model.FriendRequestStatus
import com.turnin.common.model.UserName
import com.turnin.common.model.id.DisplayId
import com.turnin.common.model.id.UserId
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateFriendRequestStatusUseCaseTest {
    private val friendRepository: FriendRepository = mockk()
    private val notificationProvider: NotificationProvider = mockk()
    private val applicationScope = TestScope()
    private val usecase = UpdateFriendRequestStatusUseCase(friendRepository, notificationProvider, applicationScope)

    @Before
    fun setUp() {
        TestDatabaseFactory.init()

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

    @After
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
        clearAllMocks()
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
        applicationScope.advanceUntilIdle()
        coVerify(exactly = 1) { notificationProvider.sendNotification(any()) }
    }

    @Test
    fun `친구 요청 거절 시 알림을 전송하지 않는다`() = runTest {
        // when
        usecase(TestUserId1.value, TestUserId2.value, FriendRequestStatus.REJECTED)

        // then
        applicationScope.advanceUntilIdle()
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
        applicationScope.advanceUntilIdle()
        coVerify(exactly = 1) { notificationProvider.sendNotification(any()) }
    }

    @Test
    fun `요청한 사용자 ID와 요청받은 사용자 ID가 같으면 예외가 발생한다`() = runTest {
        assertThrows<FriendException.SelfRequestException> {
            usecase(1L, 1L, FriendRequestStatus.ACCEPTED)
        }

        // then 로직 최상단에서 걸리므로 DB 조회가 발생하지 않아야 함
        coVerify(exactly = 0) { friendRepository.getFriendRequestContext(UserId(1L), UserId(1L)) }
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

        // then: 사용자 확인 단계에서 실패하므로 수정 로직이나 알림이 실행되지 않아야 함
        coVerify(exactly = 0) { friendRepository.updateFriendRequestStatus(TestUserId1, TestUserId2, any()) }
        applicationScope.advanceUntilIdle()
        coVerify(exactly = 0) { notificationProvider.sendNotification(any()) }
    }

    @Test
    fun `상태 수정 실패 시 알림을 전송하지 않는다`() = runTest {
        // given
        coEvery {
            friendRepository.updateFriendRequestStatus(TestUserId1, TestUserId2, any())
        } returns false

        // when
        val result = usecase(TestUserId1.value, TestUserId2.value, FriendRequestStatus.ACCEPTED)

        // then
        assertFalse(result)
        applicationScope.advanceUntilIdle()
        coVerify(exactly = 0) { notificationProvider.sendNotification(any()) }
    }

    @Test
    fun `DB 수정 로직 중 예외 발생 시 알림을 전송하지 않는다`() = runTest {
        // given
        coEvery {
            friendRepository.updateFriendRequestStatus(TestUserId1, TestUserId2, any())
        } throws RuntimeException("DB 수정을 실패했습니다.")

        // when, then
        assertThrows<RuntimeException> {
            usecase(TestUserId1.value, TestUserId2.value, FriendRequestStatus.ACCEPTED)
        }

        // then: 트랜잭션이 실패한 상태이므로 알림 로직에 진입하면 안 됨
        applicationScope.advanceUntilIdle()
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
