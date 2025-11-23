package com.peekr.domain.friend.application.usecase

import com.peekr.common.model.FriendStatus
import com.peekr.common.model.id.FriendId
import com.peekr.common.model.id.UserId
import com.peekr.domain.friend.domain.model.Friend
import com.peekr.domain.friend.domain.model.FriendshipStatus
import com.peekr.domain.friend.domain.repository.FriendRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class GetFriendshipStatusUseCaseTest {
    private val friendRepository: FriendRepository = mockk()
    private val usecase = GetFriendshipStatusUseCase(friendRepository)

    @Test
    fun `친구 데이터가 존재하지 않는다면 FriendshipStatus(NOTHING)을 반환한다`() = runTest {
        // given
        coEvery {
            friendRepository.findByIds(TestUserId, TestOtherUserId)
        } returns null

        // when
        val friendshipStatus = usecase(TestUserId, TestOtherUserId)

        // then
        assertEquals(FriendshipStatus.NOTHING, friendshipStatus)
    }

    @Test
    fun `서로 요청자, 수신자 관계이고 친구 상태가 ACCEPTED면 FriendshipStatus(Friends)을 반환한다`() = runTest {
        // given
        val friend = createFriend(TestUserId, TestOtherUserId, FriendStatus.ACCEPTED)
        coEvery {
            friendRepository.findByIds(TestUserId, TestOtherUserId)
        } returns friend

        // when
        val friendshipStatus = usecase(TestUserId, TestOtherUserId)

        // then
        assertEquals(FriendshipStatus.FRIENDS, friendshipStatus)
    }

    @Test
    fun `요청자가 본인, 수신자가 다른 사용자이고 친구 상태가 PENDING이면 FriendshipStatus(REQUESTED)을 반환한다`() = runTest {
        // given
        val friend = createFriend(TestUserId, TestOtherUserId, FriendStatus.PENDING)
        coEvery {
            friendRepository.findByIds(TestUserId, TestOtherUserId)
        } returns friend

        // when
        val friendshipStatus = usecase(TestUserId, TestOtherUserId)

        // then
        assertEquals(FriendshipStatus.REQUESTED, friendshipStatus)
    }

    @Test
    fun `요청자가 다른 사용자, 수신자가 본인이고 친구 상태가 PENDING이면 FriendshipStatus(RECEIVED)을 반환한다`() = runTest {
        // given
        val friend = createFriend(TestOtherUserId, TestUserId, FriendStatus.PENDING)
        coEvery {
            friendRepository.findByIds(TestUserId, TestOtherUserId)
        } returns friend

        // when
        val friendshipStatus = usecase(TestUserId, TestOtherUserId)

        // then
        assertEquals(FriendshipStatus.RECEIVED, friendshipStatus)
    }

    companion object {
        private val TestUserId = UserId(1L)
        private val TestOtherUserId = UserId(2L)

        private fun createFriend(
            requesterId: UserId,
            receiverId: UserId,
            status: FriendStatus,
        ) = Friend(
            id = FriendId(1L),
            requesterId = requesterId,
            receiverId = receiverId,
            status = status,
            respondedAt = null,
            createdAt = 1000,
            updatedAt = 1000,
        )
    }
}
