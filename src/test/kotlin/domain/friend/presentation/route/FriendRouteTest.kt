package com.peekr.domain.friend.presentation.route

import com.peekr.common.exception.ApiException
import com.peekr.common.exception.common.CommonErrorCode
import com.peekr.common.model.FriendStatus
import com.peekr.common.model.id.UserId
import com.peekr.common.route.Api
import com.peekr.domain.friend.application.dto.FriendDto
import com.peekr.domain.friend.application.usecase.FriendUseCases
import com.peekr.domain.friend.presentation.dto.AddFriendRequest
import com.peekr.domain.friend.presentation.dto.DeleteFriendRequest
import com.peekr.domain.friend.presentation.dto.UpdateFriendStatusRequest
import com.peekr.util.testDeleteEndpoint
import com.peekr.util.testGetEndpoint
import com.peekr.util.testPatchEndpoint
import com.peekr.util.testPlugin
import com.peekr.util.testPostEndpoint
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class FriendRouteTest {
    private val route = Api.V1.Friend
    private val usecase: FriendUseCases = mockk()

    @Before
    fun setUp() {
        coEvery { usecase.getFriends(TestUserId) } returns listOf(TestFriendDto)
        coEvery {
            usecase.add(TestRequesterId.value, TestReceiverId.value)
        } returns TestFriendDto
        coEvery {
            usecase.updateStatus(
                userId1 = TestRequesterId.value,
                userId2 = TestReceiverId.value,
                status = FriendStatus.ACCEPTED,
            )
        } returns true
        coEvery {
            usecase.delete(TestRequesterId.value, TestReceiverId.value)
        } returns true
    }

    @Test
    fun `친구 목록 조회 - 성공 테스트`() = testApplication {
        testGetEndpoint(
            endpoint = "${route.ROUTE}${route.FRIENDS}",
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { friendRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
            responseValidator = {
                containsAll(
                    TestFriendDto.requesterId.toString(),
                    TestFriendDto.receiverId.toString(),
                    TestFriendDto.status.toString(),
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
        coEvery { usecase.getFriends(TestUserId) } throws expectedApiException

        testGetEndpoint(
            endpoint = "${route.ROUTE}${route.FRIENDS}",
            queryParameters = null,
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
            queryParameters = null,
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
                    TestFriendDto.status.toString(),
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
            requestBody = TestUpdateFriendStatusRequest,
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
            requestBody = TestUpdateFriendStatusRequest.copy(requesterId = invalidRequesterId),
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
            usecase.updateStatus(TestRequesterId.value, TestReceiverId.value, FriendStatus.ACCEPTED)
        } throws expectedApiException

        testPatchEndpoint(
            endpoint = "${route.ROUTE}${route.STATUS}",
            queryParameters = null,
            requestBody = TestUpdateFriendStatusRequest,
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
            requestBody = TestUpdateFriendStatusRequest,
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
            usecase.updateStatus(TestRequesterId.value, TestReceiverId.value, FriendStatus.ACCEPTED)
        } returns false

        testPatchEndpoint(
            endpoint = "${route.ROUTE}${route.STATUS}",
            queryParameters = null,
            requestBody = TestUpdateFriendStatusRequest,
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
            queryParameters = null,
            requestBody = TestDeleteFriendRequest,
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
            queryParameters = null,
            requestBody = TestDeleteFriendRequest.copy(requesterId = 10L, receiverId = 11L),
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
            queryParameters = null,
            requestBody = TestDeleteFriendRequest,
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
            queryParameters = null,
            requestBody = TestDeleteFriendRequest,
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
            queryParameters = null,
            requestBody = TestDeleteFriendRequest,
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
            status = FriendStatus.ACCEPTED,
            respondedAt = null,
            createdAt = 1000,
            updatedAt = 1000,
        )
        private val TestAddFriendRequest = AddFriendRequest(
            requesterId = TestRequesterId.value,
            receiverId = TestReceiverId.value,
        )
        private val TestUpdateFriendStatusRequest = UpdateFriendStatusRequest(
            requesterId = TestRequesterId.value,
            receiverId = TestReceiverId.value,
            status = FriendStatus.ACCEPTED,
        )
        private val TestDeleteFriendRequest = DeleteFriendRequest(
            requesterId = TestRequesterId.value,
            receiverId = TestReceiverId.value,
        )
    }
}
