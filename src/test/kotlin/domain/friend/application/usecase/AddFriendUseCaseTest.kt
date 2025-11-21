package com.peekr.domain.friend.application.usecase

import com.peekr.common.db.DatabaseException
import com.peekr.common.model.FriendId
import com.peekr.common.model.FriendStatus
import com.peekr.common.model.UserId
import com.peekr.domain.friend.domain.model.Friend
import com.peekr.domain.friend.domain.provider.UserProvider
import com.peekr.domain.friend.domain.repository.FriendRepository
import com.peekr.domain.friend.exception.FriendException
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertThrows

class AddFriendUseCaseTest {
    private val friendRepository: FriendRepository = mockk()
    private val userProvider: UserProvider = mockk()
    private val usecase = AddFriendUseCase(friendRepository, userProvider)

    @Test
    fun `친구 추가 성공 테스트`() = runTest {
        // given
        coEvery {
            friendRepository.createFriend(TestRequesterId, TestReceiverId)
        } returns TestFriend
        coEvery {
            userProvider.existsUser(TestRequesterId)
        } returns true

        // when
        val friendDto = usecase(TestRequesterId.value, TestReceiverId.value)

        // then
        assertEquals(TestRequesterId.value, friendDto.requesterId)
        assertEquals(TestReceiverId.value, friendDto.receiverId)
    }

    @Test
    fun `요청 받을 사용자가 존재하지 않을 때 예외가 발생한다`() = runTest {
        // given
        coEvery {
            userProvider.existsUser(TestRequesterId)
        } returns false

        // when, then
        assertThrows<FriendException.UserNotFoundException> {
            usecase(TestRequesterId.value, TestReceiverId.value)
        }
    }

    @Test
    fun `친구 요청한 ID와 요청 받은 ID가 같을 때 예외가 발생한다`() = runTest {
        // given
        coEvery {
            userProvider.existsUser(TestRequesterId)
        } returns true

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
        coEvery {
            userProvider.existsUser(TestRequesterId)
        } returns true

        // when, then
        assertThrows<FriendException.AlreadyFriendRequestException> {
            usecase(TestRequesterId.value, TestReceiverId.value)
        }
    }

    companion object {
        private val TestRequesterId = UserId(1L)
        private val TestReceiverId = UserId(2L)
        private val TestFriend = Friend(
            id = FriendId(1L),
            requesterId = TestRequesterId,
            receiverId = TestReceiverId,
            status = FriendStatus.ACCEPTED,
            respondedAt = null,
            createdAt = 1000,
            updatedAt = 1000,
        )
    }
}
