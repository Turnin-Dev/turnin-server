package com.peekr.domain.friend.application.usecase

import com.peekr.common.model.FriendRequestStatus
import com.peekr.common.model.UserName
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.FriendId
import com.peekr.common.model.id.UserId
import com.peekr.common.util.pagination.offset.PaginationParams
import com.peekr.domain.friend.domain.model.IncomingRequester
import com.peekr.domain.friend.domain.model.IncomingRequesterPagingData
import com.peekr.domain.friend.domain.provider.ExternalUserInfo
import com.peekr.domain.friend.domain.provider.UserProvider
import com.peekr.domain.friend.domain.repository.FriendRepository
import com.peekr.domain.friend.exception.FriendException
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import org.junit.After

/**
 * AI 버전 테스트 코드
 */
class GetIncomingRequestersUseCaseTest {
    private val repository: FriendRepository = mockk()
    private val userProvider: UserProvider = mockk()
    private val usecase = GetIncomingRequestersUseCase(repository, userProvider)

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `받은 친구 요청 목록을 정상적으로 조회한다`() = runTest {
        // given
        val userId = 1L
        val userIdVO = UserId(userId)
        val paginationParams = PaginationParams(page = 1, size = 10)

        val requesterId1 = UserId(2L)
        val requesterId2 = UserId(3L)

        val mockRequester1 = IncomingRequester(
            id = FriendId(1L),
            requesterId = requesterId1,
            requestStatus = FriendRequestStatus.PENDING,
            respondedAt = null,
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        val mockRequester2 = IncomingRequester(
            id = FriendId(2L),
            requesterId = requesterId2,
            requestStatus = FriendRequestStatus.PENDING,
            respondedAt = null,
            createdAt = 1000L,
            updatedAt = 1000L,
        )

        val incomingRequesterPagingData = IncomingRequesterPagingData(
            totalSize = 2L,
            requesters = listOf(mockRequester1, mockRequester2),
        )

        val userInfo1 = ExternalUserInfo(
            userId = requesterId1,
            displayId = DisplayId("user2"),
            userName = UserName("사용자2"),
            profileImageUrl = "https://example.com/profile2.jpg",
        )
        val userInfo2 = ExternalUserInfo(
            userId = requesterId2,
            displayId = DisplayId("user3"),
            userName = UserName("사용자3"),
            profileImageUrl = "https://example.com/profile3.jpg",
        )

        coEvery {
            repository.getIncomingRequesters(userIdVO, 0L, 10)
        } returns incomingRequesterPagingData

        coEvery {
            userProvider.getUserInfos(listOf(requesterId1, requesterId2))
        } returns listOf(userInfo1, userInfo2)

        // when
        val result = usecase(userId, paginationParams)

        // then
        assertEquals(1, result.pagingData.pageNumber)
        assertEquals(10, result.pagingData.pageSize)
        assertEquals(2L, result.pagingData.totalSize)
        assertEquals(2, result.requesters.size)

        val requester1 = result.requesters[0]
        assertEquals(1L, requester1.id)
        assertEquals(2L, requester1.userId)
        assertEquals("user2", requester1.displayId)
        assertEquals("사용자2", requester1.name)
        assertEquals("https://example.com/profile2.jpg", requester1.profileImageUrl)

        val requester2 = result.requesters[1]
        assertEquals(2L, requester2.id)
        assertEquals(3L, requester2.userId)
        assertEquals("user3", requester2.displayId)
        assertEquals("사용자3", requester2.name)

        coVerify(exactly = 1) {
            repository.getIncomingRequesters(userIdVO, 0L, 10)
        }
        coVerify(exactly = 1) {
            userProvider.getUserInfos(listOf(requesterId1, requesterId2))
        }
    }

    @Test
    fun `받은 친구 요청이 없으면 빈 목록을 반환한다`() = runTest {
        // given
        val userId = 1L
        val userIdVO = UserId(userId)
        val paginationParams = PaginationParams(page = 1, size = 10)

        val emptyPagingData = IncomingRequesterPagingData(
            totalSize = 0L,
            requesters = emptyList(),
        )

        coEvery {
            repository.getIncomingRequesters(userIdVO, 0L, 10)
        } returns emptyPagingData

        // when
        val result = usecase(userId, paginationParams)

        // then
        assertEquals(1, result.pagingData.pageNumber)
        assertEquals(10, result.pagingData.pageSize)
        assertEquals(0L, result.pagingData.totalSize)
        assertTrue(result.requesters.isEmpty())

        coVerify(exactly = 1) {
            repository.getIncomingRequesters(userIdVO, 0L, 10)
        }
        coVerify(exactly = 0) {
            userProvider.getUserInfos(any())
        }
    }

    @Test
    fun `요청자 정보를 찾을 수 없으면 예외를 발생시킨다`() = runTest {
        // given
        val userId = 1L
        val userIdVO = UserId(userId)
        val paginationParams = PaginationParams(page = 1, size = 10)

        val requesterId = UserId(2L)

        val mockRequester = IncomingRequester(
            id = FriendId(1L),
            requesterId = requesterId,
            requestStatus = FriendRequestStatus.PENDING,
            respondedAt = null,
            createdAt = 1000L,
            updatedAt = 1000L,
        )

        val incomingRequesterPagingData = IncomingRequesterPagingData(
            totalSize = 1L,
            requesters = listOf(mockRequester),
        )

        coEvery {
            repository.getIncomingRequesters(userIdVO, 0L, 10)
        } returns incomingRequesterPagingData

        // userProvider가 빈 목록을 반환 (사용자 정보를 찾을 수 없음)
        coEvery {
            userProvider.getUserInfos(listOf(requesterId))
        } returns emptyList()

        // when & then
        assertFailsWith<FriendException.UserNotFoundException> {
            usecase(userId, paginationParams)
        }

        coVerify(exactly = 1) {
            repository.getIncomingRequesters(userIdVO, 0L, 10)
        }
        coVerify(exactly = 1) {
            userProvider.getUserInfos(listOf(requesterId))
        }
    }

    @Test
    fun `2페이지 조회 시 올바른 offset을 전달한다`() = runTest {
        // given
        val userId = 1L
        val userIdVO = UserId(userId)
        val paginationParams = PaginationParams(page = 2, size = 10)

        val emptyPagingData = IncomingRequesterPagingData(
            totalSize = 15L,
            requesters = emptyList(),
        )

        coEvery {
            repository.getIncomingRequesters(userIdVO, 10L, 10)
        } returns emptyPagingData

        // when
        val result = usecase(userId, paginationParams)

        // then
        assertEquals(2, result.pagingData.pageNumber)
        assertEquals(10, result.pagingData.pageSize)
        assertEquals(15L, result.pagingData.totalSize)

        coVerify(exactly = 1) {
            repository.getIncomingRequesters(userIdVO, 10L, 10)
        }
    }

    @Test
    fun `요청자 목록과 사용자 정보의 순서가 일치한다`() = runTest {
        // given
        val userId = 1L
        val userIdVO = UserId(userId)
        val paginationParams = PaginationParams(page = 1, size = 10)

        val requesterId1 = UserId(2L)
        val requesterId2 = UserId(3L)
        val requesterId3 = UserId(4L)

        val mockRequesters = listOf(
            IncomingRequester(
                id = FriendId(1L),
                requesterId = requesterId1,
                requestStatus = FriendRequestStatus.PENDING,
                respondedAt = null,
                createdAt = 1000L,
                updatedAt = 1000L,
            ),
            IncomingRequester(
                id = FriendId(2L),
                requesterId = requesterId2,
                requestStatus = FriendRequestStatus.PENDING,
                respondedAt = null,
                createdAt = 1000L,
                updatedAt = 1000L,
            ),
            IncomingRequester(
                id = FriendId(3L),
                requesterId = requesterId3,
                requestStatus = FriendRequestStatus.PENDING,
                respondedAt = null,
                createdAt = 1000L,
                updatedAt = 1000L,
            ),
        )

        val incomingRequesterPagingData = IncomingRequesterPagingData(
            totalSize = 3L,
            requesters = mockRequesters,
        )

        // UserProvider는 역순으로 반환
        val userInfos = listOf(
            ExternalUserInfo(
                userId = requesterId3,
                displayId = DisplayId("user4"),
                userName = UserName("사용자4"),
                profileImageUrl = null,
            ),
            ExternalUserInfo(
                userId = requesterId1,
                displayId = DisplayId("user2"),
                userName = UserName("사용자2"),
                profileImageUrl = null,
            ),
            ExternalUserInfo(
                userId = requesterId2,
                displayId = DisplayId("user3"),
                userName = UserName("사용자3"),
                profileImageUrl = null,
            ),
        )

        coEvery {
            repository.getIncomingRequesters(userIdVO, 0L, 10)
        } returns incomingRequesterPagingData

        coEvery {
            userProvider.getUserInfos(listOf(requesterId1, requesterId2, requesterId3))
        } returns userInfos

        // when
        val result = usecase(userId, paginationParams)

        // then
        assertEquals(3, result.requesters.size)
        // Repository에서 반환한 순서대로 매핑되어야 함
        assertEquals(2L, result.requesters[0].userId)
        assertEquals(3L, result.requesters[1].userId)
        assertEquals(4L, result.requesters[2].userId)
    }
}
