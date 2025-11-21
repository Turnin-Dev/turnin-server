package com.peekr.domain.friend.application.usecase

import com.peekr.common.model.FriendId
import com.peekr.common.model.FriendStatus
import com.peekr.common.model.UserId
import com.peekr.domain.friend.domain.model.Friend
import com.peekr.domain.friend.domain.repository.FriendRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class GetFriendsUseCaseTest {
    private val friendRepository: FriendRepository = mockk()
    private val usecase = GetFriendsUseCase(friendRepository)

    @Test
    fun `친구 목록 조회 성공 테스트`() = runTest {
        // given
        coEvery {
            friendRepository.getFriends(TestFriend.requesterId)
        } returns listOf(TestFriend)

        // when
        val result = usecase(TestFriend.requesterId)

        // then
        assertTrue(result.isNotEmpty())
    }

    companion object {
        private val TestFriend = Friend(
            id = FriendId(1L),
            requesterId = UserId(1L),
            receiverId = UserId(2L),
            status = FriendStatus.ACCEPTED,
            respondedAt = null,
            createdAt = 1000,
            updatedAt = 1000,
        )
    }
}
