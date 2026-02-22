package com.peekr.domain.friend.application.usecase

import com.peekr.common.db.DatabaseException
import com.peekr.common.model.FriendRequestStatus
import com.peekr.common.model.id.FriendId
import com.peekr.common.model.id.UserId
import com.peekr.domain.friend.domain.model.Friend
import com.peekr.domain.friend.domain.repository.FriendRepository
import com.peekr.domain.friend.exception.FriendException
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class AddFriendUseCaseTest {
    private val friendRepository: FriendRepository = mockk()
    private val usecase = AddFriendUseCase(friendRepository)

    @Before
    fun setup() {
        coEvery {
            friendRepository.isBlockedRelationship(TestRequesterId, TestReceiverId)
        } returns false
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
        coEvery {
            friendRepository.existsUser(TestReceiverId)
        } returns true

        // when
        val friendDto = usecase(
            requesterId = TestRequesterId.value,
            receiverId = TestReceiverId.value,
        )

        // then
        assertEquals(TestRequesterId.value, friendDto.requesterId)
        assertEquals(TestReceiverId.value, friendDto.receiverId)
    }

    @Test
    fun `요청 받을 사용자가 존재하지 않을 때 예외가 발생한다`() = runTest {
        // given
        coEvery {
            friendRepository.existsUser(TestReceiverId)
        } returns false

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
        // given
        coEvery {
            friendRepository.isBlockedRelationship(UserId(1L), UserId(1L))
        } returns false

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
            friendRepository.existsUser(TestReceiverId)
        } returns true

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
            friendRepository.isBlockedRelationship(TestRequesterId, TestReceiverId)
        } returns true

        // when, then
        assertThrows<FriendException.UserNotFoundException> {
            usecase(
                requesterId = TestRequesterId.value,
                receiverId = TestReceiverId.value,
            )
        }
    }

    companion object {
        private val TestOwnerId = UserId(1L)
        private val TestRequesterId = UserId(1L)
        private val TestReceiverId = UserId(2L)
        private val TestFriend = Friend(
            id = FriendId(1L),
            requesterId = TestRequesterId,
            receiverId = TestReceiverId,
            requestStatus = FriendRequestStatus.ACCEPTED,
            respondedAt = null,
            createdAt = 1000,
            updatedAt = 1000,
        )
    }
}
