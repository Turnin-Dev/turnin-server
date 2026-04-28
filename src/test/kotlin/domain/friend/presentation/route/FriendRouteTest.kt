package com.turnin.domain.friend.presentation.route

import com.turnin.common.exception.ApiException
import com.turnin.common.exception.common.CommonErrorCode
import com.turnin.common.jwt.JWTTestDoubles
import com.turnin.common.model.FriendRequestStatus
import com.turnin.common.model.id.UserId
import com.turnin.common.route.Api
import com.turnin.common.util.pagination.offset.PaginationParams
import com.turnin.common.util.pagination.offset.PagingData
import com.turnin.domain.friend.application.dto.FriendDto
import com.turnin.domain.friend.application.dto.FriendInfoDto
import com.turnin.domain.friend.application.dto.FriendsPagingDataDto
import com.turnin.domain.friend.application.dto.IncomingRequestInfoDto
import com.turnin.domain.friend.application.dto.IncomingRequestPagingDataDto
import com.turnin.domain.friend.application.usecase.FriendUseCases
import com.turnin.domain.friend.presentation.dto.AddFriendRequest
import com.turnin.domain.friend.presentation.dto.FriendsResponse
import com.turnin.domain.friend.presentation.dto.IncomingRequestsResponse
import com.turnin.domain.friend.presentation.dto.UpdateFriendStatusRequest
import com.turnin.util.testDeleteEndpoint
import com.turnin.util.testGetEndpoint
import com.turnin.util.testPaginationRoute
import com.turnin.util.testPatchEndpoint
import com.turnin.util.testPlugin
import com.turnin.util.testPostEndpoint
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Test

class FriendRouteTest {
    private val route = Api.V1.Friend
    private val usecase: FriendUseCases = mockk()

    @Before
    fun setUp() {
        coEvery {
            usecase.add(TestRequesterId.value, TestReceiverId.value)
        } returns TestFriendDto
        coEvery {
            usecase.updateStatus(
                requesterId = TestRequesterId.value,
                receiverId = TestReceiverId.value,
                requestStatus = FriendRequestStatus.ACCEPTED,
            )
        } returns true
        coEvery {
            usecase.delete(TestRequesterId.value, TestReceiverId.value)
        } returns true
    }

    @Test
    fun `친구 목록 조회 - 페이지네이션 테스트`() = testApplication {
        testPlugin(
            authRouting = { friendRoutes(route, usecase) },
        )

        // given: 설정
        val pageSize = 10
        val totalItems = 100L // 전체 아이템 수 (10페이지 분량)
        val totalPages = (totalItems / pageSize).toInt() // 총 페이지 수: 10
        val testUserId = TestUserId.value
        val token = JWTTestDoubles.getMockJWTToken(testUserId.toString())

        // given: 데이터 Mocking
        // 1. 일반 페이지 (1페이지 ~ 10페이지) Mocking
        repeat(totalPages) { pageIndex ->
            val pageNumber = pageIndex + 1L
            val testFriendsPagingDataDto = FriendsPagingDataDto(
                pagingData = PagingData(
                    pageNumber = pageNumber,
                    pageSize = pageSize,
                    totalSize = totalItems,
                ),
                friends = List(pageSize) { TestFriendInfoDto },
            )
            val paginationParams = PaginationParams(pageNumber, pageSize)
            coEvery {
                usecase.getFriends(testUserId, paginationParams)
            } returns testFriendsPagingDataDto
        }

        // 2. 마지막 페이지를 넘어서는 요청 (11페이지) Mocking
        val lastPageNumber = (totalPages + 1).toLong()
        coEvery {
            usecase.getFriends(testUserId, PaginationParams(lastPageNumber, pageSize))
        } returns FriendsPagingDataDto(
            pagingData = PagingData(
                pageNumber = lastPageNumber,
                pageSize = pageSize,
                totalSize = totalItems,
            ),
            friends = emptyList(),
        )

        // when, then: 페이지네이션 시나리오 테스트
        // 1. 일반 페이지 (1 ~ 9페이지) 검증
        (1 until totalPages).forEach { pageNumber ->
            testPaginationRoute<FriendsResponse>(
                endpoint = "${route.ROUTE}${route.FRIENDS}",
                queryParameters = mapOf(
                    "userId" to "$testUserId",
                    "page" to "$pageNumber",
                    "size" to "$pageSize",
                ),
                token = token,
                assertion = { response ->
                    assertEquals(pageSize, response.friends.size)
                    assertTrue(response.hasNext)
                },
            )
        }

        // 2. 마지막 페이지 (10페이지) 검증
        testPaginationRoute<FriendsResponse>(
            endpoint = "${route.ROUTE}${route.FRIENDS}",
            queryParameters = mapOf(
                "userId" to "$testUserId",
                "page" to "$totalPages",
                "size" to "$pageSize",
            ),
            token = token,
            assertion = { response ->
                assertEquals(pageSize, response.friends.size)
                assertFalse(response.hasNext)
            },
        )

        // 3. 존재하지 않는 페이지 (11페이지) 검증
        testPaginationRoute<FriendsResponse>(
            endpoint = "${route.ROUTE}${route.FRIENDS}",
            queryParameters = mapOf(
                "userId" to "$testUserId",
                "page" to "${totalPages + 1}",
                "size" to "$pageSize",
            ),
            token = token,
            assertion = { response ->
                assertEquals(0, response.friends.size)
                assertFalse(response.hasNext)
            },
        )
    }

    @Test
    fun `친구 목록 조회 - 성공 테스트`() = testApplication {
        // given
        coEvery {
            usecase.getFriends(TestUserId.value, any())
        } returns TestFriendsPagingDataDto

        // when, then
        testGetEndpoint(
            endpoint = "${route.ROUTE}${route.FRIENDS}",
            queryParameters = mapOf(
                "userId" to "${TestUserId.value}",
                "page" to "1",
                "size" to "10",
            ),
            testPlugin = {
                testPlugin(
                    authRouting = { friendRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
            responseValidator = {
                containsAll(
                    TestFriendInfoDto.id.toString(),
                    TestFriendInfoDto.userId.toString(),
                    TestFriendInfoDto.name,
                )
            },
        )
    }

    @Test
    fun `친구 목록 조회 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        val expectedApiException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        coEvery { usecase.getFriends(TestUserId.value, any()) } throws expectedApiException

        testGetEndpoint(
            endpoint = "${route.ROUTE}${route.FRIENDS}",
            queryParameters = mapOf(
                "userId" to "${TestUserId.value}",
                "page" to "1",
                "size" to "10",
            ),
            testPlugin = {
                testPlugin(
                    authRouting = { friendRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = expectedApiException.status,
            responseValidator = {
                containsAll(
                    expectedApiException.errorCode.code,
                    expectedApiException.errorCode.description,
                )
            },
        )
    }

    @Test
    fun `친구 목록 조회 - 토큰 없이 요청 시 401 에러를 반환한다`() = testApplication {
        testGetEndpoint(
            endpoint = "${route.ROUTE}${route.FRIENDS}",
            queryParameters = mapOf(
                "userId" to "${TestUserId.value}",
                "page" to "1",
                "size" to "10",
            ),
            testPlugin = {
                testPlugin(
                    authRouting = { friendRoutes(route, usecase) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    @Test
    fun `받은 친구 요청 목록 조회 - 페이지네이션 테스트`() = testApplication {
        testPlugin(
            authRouting = { friendRoutes(route, usecase) },
        )

        // given: 설정
        val pageSize = 10
        val totalItems = 100L // 전체 아이템 수 (10페이지 분량)
        val totalPages = (totalItems / pageSize).toInt() // 총 페이지 수: 10
        val testUserId = TestUserId.value
        val token = JWTTestDoubles.getMockJWTToken(testUserId.toString())

        // given: 데이터 Mocking
        // 1. 일반 페이지 (1페이지 ~ 10페이지) Mocking
        repeat(totalPages) { pageIndex ->
            val pageNumber = pageIndex + 1L
            val pagingDataDto = IncomingRequestPagingDataDto(
                pagingData = PagingData(
                    pageNumber = pageNumber,
                    pageSize = pageSize,
                    totalSize = totalItems,
                ),
                requests = List(pageSize) { createIncomingRequestInfoDto(it + 1L) },
            )
            val paginationParams = PaginationParams(pageNumber, pageSize)
            coEvery {
                usecase.getIncomingRequesters(testUserId, paginationParams)
            } returns pagingDataDto
        }

        // 2. 마지막 페이지를 넘어서는 요청 (11페이지) Mocking
        val lastPageNumber = (totalPages + 1).toLong()
        coEvery {
            usecase.getIncomingRequesters(testUserId, PaginationParams(lastPageNumber, pageSize))
        } returns IncomingRequestPagingDataDto(
            pagingData = PagingData(
                pageNumber = lastPageNumber,
                pageSize = pageSize,
                totalSize = totalItems,
            ),
            requests = emptyList(),
        )

        // when, then: 페이지네이션 시나리오 테스트
        // 1. 일반 페이지 (1 ~ 9페이지) 검증
        (1 until totalPages).forEach { pageNumber ->
            testPaginationRoute<IncomingRequestsResponse>(
                endpoint = "${route.ROUTE}${route.INCOMING_REQUEST}",
                queryParameters = mapOf(
                    "userId" to "$testUserId",
                    "page" to "$pageNumber",
                    "size" to "$pageSize",
                ),
                token = token,
                assertion = { response ->
                    assertEquals(pageSize, response.requesters.size)
                    assertTrue(response.hasNext)
                },
            )
        }

        // 2. 마지막 페이지 (10페이지) 검증
        testPaginationRoute<IncomingRequestsResponse>(
            endpoint = "${route.ROUTE}${route.INCOMING_REQUEST}",
            queryParameters = mapOf(
                "userId" to "$testUserId",
                "page" to "$totalPages",
                "size" to "$pageSize",
            ),
            token = token,
            assertion = { response ->
                assertEquals(pageSize, response.requesters.size)
                assertFalse(response.hasNext)
            },
        )

        // 3. 존재하지 않는 페이지 (11페이지) 검증
        testPaginationRoute<IncomingRequestsResponse>(
            endpoint = "${route.ROUTE}${route.INCOMING_REQUEST}",
            queryParameters = mapOf(
                "userId" to "$testUserId",
                "page" to "${totalPages + 1}",
                "size" to "$pageSize",
            ),
            token = token,
            assertion = { response ->
                assertEquals(0, response.requesters.size)
                assertFalse(response.hasNext)
            },
        )
    }

    @Test
    fun `받은 친구 요청 목록 조회 - 성공 테스트`() = testApplication {
        // given
        coEvery {
            usecase.getIncomingRequesters(TestUserId.value, any())
        } returns TestIncomingRequestInfoDto

        // when, then
        testGetEndpoint(
            endpoint = "${route.ROUTE}${route.INCOMING_REQUEST}",
            queryParameters = mapOf(
                "userId" to "${TestUserId.value}",
                "page" to "1",
                "size" to "10",
            ),
            testPlugin = {
                testPlugin(
                    authRouting = { friendRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
            responseValidator = {
                containsAll(
                    TestIncomingRequestInfoDto.requests
                        .first()
                        .userId
                        .toString(),
                    TestIncomingRequestInfoDto.requests.first().name,
                    TestIncomingRequestInfoDto.requests.first().displayId,
                )
            },
        )
    }

    @Test
    fun `받은 친구 요청 목록 조회 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        val expectedApiException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        coEvery {
            usecase.getIncomingRequesters(TestUserId.value, any())
        } throws expectedApiException

        testGetEndpoint(
            endpoint = "${route.ROUTE}${route.INCOMING_REQUEST}",
            queryParameters = mapOf(
                "userId" to "${TestUserId.value}",
                "page" to "1",
                "size" to "10",
            ),
            testPlugin = {
                testPlugin(
                    authRouting = { friendRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = expectedApiException.status,
            responseValidator = {
                containsAll(
                    expectedApiException.errorCode.code,
                    expectedApiException.errorCode.description,
                )
            },
        )
    }

    @Test
    fun `받은 친구 요청 목록 조회 - 토큰 없이 요청 시 401 에러를 반환한다`() = testApplication {
        testGetEndpoint(
            endpoint = "${route.ROUTE}${route.INCOMING_REQUEST}",
            queryParameters = mapOf(
                "userId" to "${TestUserId.value}",
                "page" to "1",
                "size" to "10",
            ),
            testPlugin = {
                testPlugin(
                    authRouting = { friendRoutes(route, usecase) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    @Test
    fun `친구 추가 - 성공 테스트`() = testApplication {
        testPostEndpoint(
            endpoint = route.ROUTE,
            queryParameters = null,
            requestBody = TestAddFriendRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { friendRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.Created,
            responseValidator = {
                containsAll(
                    TestFriendDto.requesterId.toString(),
                    TestFriendDto.receiverId.toString(),
                    TestFriendDto.requestStatus.toString(),
                )
            },
        )
    }

    @Test
    fun `친구 추가 - 인증된 사용자 ID와 requesterId가 같지 않은 경우 Forbidden을 반환한다`() = testApplication {
        val invalidRequesterId = 10L

        testPostEndpoint(
            endpoint = route.ROUTE,
            queryParameters = null,
            requestBody = TestAddFriendRequest.copy(requesterId = invalidRequesterId),
            testPlugin = {
                testPlugin(
                    authRouting = { friendRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.Forbidden,
        )
    }

    @Test
    fun `친구 추가 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        val expectedApiException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        coEvery {
            usecase.add(TestRequesterId.value, TestReceiverId.value)
        } throws expectedApiException

        testPostEndpoint(
            endpoint = route.ROUTE,
            queryParameters = null,
            requestBody = TestAddFriendRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { friendRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = expectedApiException.status,
            responseValidator = {
                containsAll(
                    expectedApiException.errorCode.code,
                    expectedApiException.errorCode.description,
                )
            },
        )
    }

    @Test
    fun `친구 추가 - 토큰 없이 요청 시 401 에러를 반환한다`() = testApplication {
        testPostEndpoint(
            endpoint = route.ROUTE,
            queryParameters = null,
            requestBody = TestAddFriendRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { friendRoutes(route, usecase) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    @Test
    fun `친구 상태 수정 - 성공 테스트`() = testApplication {
        testPatchEndpoint(
            endpoint = "${route.ROUTE}${route.STATUS}",
            queryParameters = null,
            requestBody = TestUpdateFriendRequestStatusRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { friendRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
        )
    }

    @Test
    fun `친구 상태 수정 - 인증된 사용자 ID와 requesterId가 같지 않은 경우 Forbidden을 반환한다`() = testApplication {
        val invalidRequesterId = 10L

        testPatchEndpoint(
            endpoint = "${route.ROUTE}${route.STATUS}",
            queryParameters = null,
            requestBody = TestUpdateFriendRequestStatusRequest.copy(requesterId = invalidRequesterId),
            testPlugin = {
                testPlugin(
                    authRouting = { friendRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.Forbidden,
        )
    }

    @Test
    fun `친구 상태 수정 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        val expectedApiException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        coEvery {
            usecase.updateStatus(TestRequesterId.value, TestReceiverId.value, FriendRequestStatus.ACCEPTED)
        } throws expectedApiException

        testPatchEndpoint(
            endpoint = "${route.ROUTE}${route.STATUS}",
            queryParameters = null,
            requestBody = TestUpdateFriendRequestStatusRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { friendRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = expectedApiException.status,
            responseValidator = {
                containsAll(
                    expectedApiException.errorCode.code,
                    expectedApiException.errorCode.description,
                )
            },
        )
    }

    @Test
    fun `친구 상태 수정 - 토큰 없이 요청 시 401 에러를 반환한다`() = testApplication {
        testPatchEndpoint(
            endpoint = "${route.ROUTE}${route.STATUS}",
            queryParameters = null,
            requestBody = TestUpdateFriendRequestStatusRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { friendRoutes(route, usecase) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    @Test
    fun `친구 상태 수정 - 수정 실패 시 수정할 데이터가 없다는 것으로 간주하고 NotFound를 반환한다`() = testApplication {
        coEvery {
            usecase.updateStatus(TestRequesterId.value, TestReceiverId.value, FriendRequestStatus.ACCEPTED)
        } returns false

        testPatchEndpoint(
            endpoint = "${route.ROUTE}${route.STATUS}",
            queryParameters = null,
            requestBody = TestUpdateFriendRequestStatusRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { friendRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.NotFound,
        )
    }

    @Test
    fun `친구 삭제 - 성공 테스트`() = testApplication {
        testDeleteEndpoint(
            endpoint = route.ROUTE,
            queryParameters = mapOf(
                "requesterId" to "${TestRequesterId.value}",
                "receiverId" to "${TestReceiverId.value}",
            ),
            testPlugin = {
                testPlugin(
                    authRouting = { friendRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
        )
    }

    @Test
    fun `친구 삭제 - 인증된 사용자 ID가 requesterId, receiveId 둘 중 아무와도 일치하지 않는 경우 Forbidden을 반환한다`() = testApplication {
        testDeleteEndpoint(
            endpoint = route.ROUTE,
            queryParameters = mapOf(
                "requesterId" to "10",
                "receiverId" to "11",
            ),
            testPlugin = {
                testPlugin(
                    authRouting = { friendRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.Forbidden,
        )
    }

    @Test
    fun `친구 삭제 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        val expectedApiException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        coEvery {
            usecase.delete(TestRequesterId.value, TestReceiverId.value)
        } throws expectedApiException

        testDeleteEndpoint(
            endpoint = route.ROUTE,
            queryParameters = mapOf(
                "requesterId" to "${TestRequesterId.value}",
                "receiverId" to "${TestReceiverId.value}",
            ),
            testPlugin = {
                testPlugin(
                    authRouting = { friendRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = expectedApiException.status,
            responseValidator = {
                containsAll(
                    expectedApiException.errorCode.code,
                    expectedApiException.errorCode.description,
                )
            },
        )
    }

    @Test
    fun `친구 삭제 - 토큰 없이 요청 시 401 에러를 반환한다`() = testApplication {
        testDeleteEndpoint(
            endpoint = route.ROUTE,
            queryParameters = mapOf(
                "requesterId" to "${TestRequesterId.value}",
                "receiverId" to "${TestReceiverId.value}",
            ),
            testPlugin = {
                testPlugin(
                    authRouting = { friendRoutes(route, usecase) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    @Test
    fun `친구 삭제 - 삭제 실패 시 수정할 데이터가 없다는 것으로 간주하고 NotFound를 반환한다`() = testApplication {
        coEvery {
            usecase.delete(TestRequesterId.value, TestReceiverId.value)
        } returns false

        testDeleteEndpoint(
            endpoint = route.ROUTE,
            queryParameters = mapOf(
                "requesterId" to "${TestRequesterId.value}",
                "receiverId" to "${TestReceiverId.value}",
            ),
            testPlugin = {
                testPlugin(
                    authRouting = { friendRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.NotFound,
        )
    }

    companion object {
        private val TestUserId = UserId(1L)
        private val TestRequesterId = UserId(1L)
        private val TestReceiverId = UserId(2L)
        private val TestFriendDto = FriendDto(
            id = 1L,
            requesterId = TestRequesterId.value,
            receiverId = TestReceiverId.value,
            requestStatus = FriendRequestStatus.ACCEPTED,
            respondedAt = null,
            createdAt = 1000,
            updatedAt = 1000,
        )
        private val TestAddFriendRequest = AddFriendRequest(
            requesterId = TestRequesterId.value,
            receiverId = TestReceiverId.value,
        )
        private val TestUpdateFriendRequestStatusRequest = UpdateFriendStatusRequest(
            requesterId = TestRequesterId.value,
            receiverId = TestReceiverId.value,
            requestStatus = FriendRequestStatus.ACCEPTED,
        )
        private val TestFriendInfoDto = FriendInfoDto(
            id = 1L,
            userId = 1L,
            displayId = "did",
            name = "name",
            profileImageUrl = null,
            respondedAt = 1000,
            createdAt = 1000,
            updatedAt = 1000,
        )
        private val TestFriendsPagingDataDto = FriendsPagingDataDto(
            pagingData = PagingData(
                pageNumber = 1,
                pageSize = 10,
                totalSize = 100,
            ),
            friends = listOf(TestFriendInfoDto),
        )
        private val TestIncomingRequestInfoDto = IncomingRequestPagingDataDto(
            pagingData = PagingData(
                pageNumber = 1,
                pageSize = 10,
                totalSize = 100,
            ),
            requests = listOf(createIncomingRequestInfoDto(1)),
        )

        private fun createIncomingRequestInfoDto(uniqueValue: Long): IncomingRequestInfoDto =
            IncomingRequestInfoDto(
                id = uniqueValue,
                userId = uniqueValue,
                displayId = "did$uniqueValue",
                name = "name$uniqueValue",
                profileImageUrl = null,
                respondedAt = 1000L,
                createdAt = 1000L,
                updatedAt = 1000L,
            )
    }
}
