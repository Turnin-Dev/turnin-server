package com.peekr.domain.friend.application.usecase

import com.peekr.common.model.FriendRequestStatus
import com.peekr.common.model.UserName
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.FriendId
import com.peekr.common.model.id.UserId
import com.peekr.common.util.pagination.offset.PaginationParams
import com.peekr.domain.friend.domain.model.Friend
import com.peekr.domain.friend.domain.model.FriendsPagingData
import com.peekr.domain.friend.domain.model.UserInfo
import com.peekr.domain.friend.domain.repository.FriendRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class GetFriendsUseCaseTest {
    private val friendRepository: FriendRepository = mockk()
    private val usecase = GetFriendsUseCase(friendRepository)

    @Test
    fun `친구 목록 페이지네이션 조회 성공 테스트`() = runTest {
        // given: 100개의 항목에 대한 페이지네이션 Mock 데이터 설정
        val totalSize = 100
        val pageSize = 10
        val expectedPageSize = (totalSize / pageSize).toLong()

        // 0. 사용자 정보 조회 데이터
        coEvery { friendRepository.getUserInfos(any()) } answers {
            val ids = firstArg<List<UserId>>()
            ids.map { id ->
                TestUserInfo.copy(userId = id)
            }
        }

        // 1. 10개 페이지에 대한 데이터
        repeat(expectedPageSize.toInt()) {
            val pageNumber = (it + 1).toLong()
            val paginationParams = PaginationParams(
                page = pageNumber,
                size = pageSize,
            )
            val friendsPagingData = FriendsPagingData(100, List(pageSize) { TestFriend })
            coEvery {
                friendRepository.getFriendsPagination(
                    userId = TestFriend.requesterId,
                    offset = paginationParams.offset,
                    size = paginationParams.size,
                )
            } returns friendsPagingData
        }

        // 2. 존재하지 않는 11페이지에 대한 데이터
        val tesPaginationParams = PaginationParams(page = (expectedPageSize + 1), size = pageSize)
        coEvery {
            friendRepository.getFriendsPagination(
                userId = TestFriend.requesterId,
                offset = tesPaginationParams.offset,
                size = tesPaginationParams.size,
            )
        } returns FriendsPagingData(100, emptyList())

        // when, then: 10개의 페이지에 대한 테스트 및 검증 수행
        // 1. 1페이지 ~ 9페이지
        for (pageNumber in 1 until expectedPageSize) {
            val paginationParams = PaginationParams(
                page = pageNumber,
                size = pageSize,
            )
            val result = usecase(
                userId = TestFriend.requesterId.value,
                paginationParams = paginationParams,
            )

            assertEquals(pageNumber, result.pagingData.pageNumber)
            assertTrue(result.pagingData.hasNext, "[1 ~ 9페이지]: 페이지 hasNext 불일치")
            assertEquals(pageSize, result.friends.size)
        }

        // 2. 10 페이지
        val result = usecase(
            userId = TestFriend.requesterId.value,
            paginationParams = PaginationParams(
                page = expectedPageSize,
                size = pageSize,
            ),
        )
        assertEquals(expectedPageSize, result.pagingData.pageNumber)
        assertFalse(result.pagingData.hasNext, "[10 페이지]: 페이지 hasNext 불일치")

        // 3. 11 페이지 (존재하지 않는 페이지)
        val notExistPageNumber = expectedPageSize + 1
        val result2 = usecase(
            userId = TestFriend.requesterId.value,
            paginationParams = PaginationParams(
                page = notExistPageNumber,
                size = pageSize,
            ),
        )
        assertEquals(notExistPageNumber, result2.pagingData.pageNumber)
        assertFalse(result2.pagingData.hasNext)
        assertTrue(result2.friends.isEmpty(), "[11페이지(존재하지 않는 페이지)]: 페이지 hasNext 불일치")
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
        private val TestUserInfo = UserInfo(
            userId = UserId(1L),
            displayId = DisplayId("did"),
            userName = UserName("name"),
            profileImageUrl = null,
        )
    }
}
