package com.peekr.domain.friend.application.usecase

import com.peekr.common.model.FriendRequestStatus
import com.peekr.common.model.id.UserId
import com.peekr.domain.friend.domain.repository.FriendRepository
import com.peekr.domain.friend.exception.FriendException
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertThrows

class UpdateFriendStatusUseCaseTest {
    private val friendRepository: FriendRepository = mockk()
    private val usecase = UpdateFriendRequestStatusUseCase(friendRepository)

    @BeforeTest
    fun setUp() {
        coEvery { friendRepository.existsUser(TestUserId1) } returns true
        coEvery { friendRepository.existsUser(TestUserId2) } returns true
        coEvery {
            friendRepository.updateFriendRequestStatus(TestUserId1, TestUserId2, any())
        } returns true
    }

    @Test
    fun `친구 상태 수정 성공 테스트`() = runTest {
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
    fun `상태를 수정하려는 친구(사용자)가 존재하지 않는 경우 예외가 발생한다`() = runTest {
        // given
        coEvery { friendRepository.existsUser(TestUserId2) } returns false

        // when, then
        assertThrows<FriendException.UserNotFoundException> {
            usecase(TestUserId1.value, TestUserId2.value, FriendRequestStatus.ACCEPTED)
        }
    }

    companion object {
        private val TestUserId1 = UserId(1L)
        private val TestUserId2 = UserId(2L)
    }
}
