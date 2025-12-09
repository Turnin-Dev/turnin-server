package com.peekr.domain.friend.application.usecase

import com.peekr.common.model.FriendRequestStatus
import com.peekr.common.model.id.FriendId
import com.peekr.common.model.id.UserId
import com.peekr.common.util.pagination.PaginationParams
import com.peekr.domain.friend.application.dto.toDto
import com.peekr.domain.friend.domain.model.Friend
import com.peekr.domain.friend.domain.model.FriendsPagingData
import com.peekr.domain.friend.domain.repository.FriendRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class GetFriendsUseCaseTest {
    private val friendRepository: FriendRepository = mockk()
    private val usecase = GetFriendsUseCase(friendRepository)

    @Test
    fun `친구 목록 조회 성공 테스트`() = runTest {
        // given
        coEvery {
            friendRepository.getFriendsPagination(
                userId = TestFriend.requesterId,
                offset = TestPaginationParams.offset,
                size = TestPaginationParams.size,
            )
        } returns TestFriendsPagingData

        // when
        val result = usecase(
            userId = TestFriend.requesterId.value,
            offset = TestPaginationParams.offset,
            size = TestPaginationParams.size,
        )

        // then
        assertEquals(TestFriendsPagingData.totalSize, result.pagingData.totalSize)
        assertEquals(TestFriendsPagingData.friends.map { it.toDto() }, result.friends)
    }

    companion object {
        private val TestFriend = Friend(
            id = FriendId(1L),
            requesterId = UserId(1L),
            receiverId = UserId(2L),
            requestStatus = FriendRequestStatus.ACCEPTED,
            respondedAt = null,
            createdAt = 1000,
            updatedAt = 1000,
        )
        private val TestFriendsPagingData = FriendsPagingData(100, listOf(TestFriend))
        private val TestPaginationParams = PaginationParams(1, 10)
    }
}
