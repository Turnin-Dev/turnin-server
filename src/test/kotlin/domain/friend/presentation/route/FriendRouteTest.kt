package com.peekr.domain.friend.presentation.route

import com.peekr.common.exception.ApiException
import com.peekr.common.exception.common.CommonErrorCode
import com.peekr.common.jwt.JWTTestDoubles
import com.peekr.common.model.FriendRequestStatus
import com.peekr.common.model.id.UserId
import com.peekr.common.route.Api
import com.peekr.common.util.pagination.PaginationParams
import com.peekr.common.util.pagination.PagingData
import com.peekr.domain.friend.application.dto.FriendDto
import com.peekr.domain.friend.application.dto.FriendsPagingDataDto
import com.peekr.domain.friend.application.usecase.FriendUseCases
import com.peekr.domain.friend.presentation.dto.AddFriendRequest
import com.peekr.domain.friend.presentation.dto.FriendsResponse
import com.peekr.domain.friend.presentation.dto.UpdateFriendStatusRequest
import com.peekr.util.TestClientFactory.createTestClient
import com.peekr.util.testDeleteEndpoint
import com.peekr.util.testGetEndpoint
import com.peekr.util.testPatchEndpoint
import com.peekr.util.testPlugin
import com.peekr.util.testPostEndpoint
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
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
                userId1 = TestRequesterId.value,
                userId2 = TestReceiverId.value,
                requestStatus = FriendRequestStatus.ACCEPTED,
            )
        } returns true
        coEvery {
            usecase.delete(TestRequesterId.value, TestReceiverId.value)
        } returns true
    }

    @Test
    fun `친구 목록 조회 - 페이지네이션 테스트`() = testApplication {
        // given: 데이터 모킹
        val pageSize = 10
        val totalSize = 100L
        repeat(10) { pageNumber ->
            val testFriendsPagingDataDto = FriendsPagingDataDto(
                pagingData = PagingData(
                    pageNumber = (pageNumber + 1).toLong(),
                    pageSize = pageSize,
                    totalSize = totalSize,
                ),
                friends = List(pageSize) { TestFriendDto },
            )
            val testPaginationParams = PaginationParams((pageNumber + 1).toLong(), pageSize)
            coEvery {
                usecase.getFriendsPagination(TestUserId.value, testPaginationParams)
            } returns testFriendsPagingDataDto
        }
        // 마지막 페이지는 빈 리스트 반환
        coEvery {
            usecase.getFriendsPagination(TestUserId.value, PaginationParams(11, pageSize))
        } returns FriendsPagingDataDto(
            pagingData = PagingData(
                pageNumber = 11,
                pageSize = pageSize,
                totalSize = totalSize,
            ),
            friends = emptyList(),
        )

        // given: 설정
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        testPlugin(
            authRouting = { friendRoutes(route, usecase) },
        )

        // when, then: 10번의 요청을 하고 매번 검증을 수행한다.
        repeat(11) { idx ->
            val pageNumber = idx + 1
            // when
            val response = client.get("${route.ROUTE}${route.FRIENDS}") {
                // 쿼리 파라미터
                url {
                    mapOf(
                        "userId" to "${TestUserId.value}",
                        "page" to "$pageNumber",
                        "size" to "$pageSize",
                    ).forEach { (key, value) ->
                        parameters.append(key, value)
                    }
                }
                // 인증 헤더
                header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            }

            // then
            val responseBody = response.bodyAsText()
            val friendsResponse = Json.decodeFromString<FriendsResponse>(responseBody)

            if (pageNumber < 11) {
                assertEquals(pageSize, friendsResponse.friends.size)
                if (pageNumber == 10) {
                    assertFalse(friendsResponse.hasNext, "hasNext should be false: $responseBody")
                } else {
                    assertTrue(friendsResponse.hasNext, "hasNext should be true: $responseBody")
                }
            }

            if (pageNumber == 11) {
                assertTrue(friendsResponse.friends.isEmpty())
                assertFalse(friendsResponse.hasNext, "hasNext should be false: $responseBody")
            }
        }
    }

    @Test
    fun `친구 목록 조회 - 성공 테스트`() = testApplication {
        // given
        coEvery {
            usecase.getFriendsPagination(TestUserId.value, any())
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
                    TestFriendDto.requesterId.toString(),
                    TestFriendDto.receiverId.toString(),
                    TestFriendDto.requestStatus.toString(),
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
        coEvery { usecase.getFriendsPagination(TestUserId.value, any()) } throws expectedApiException

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
        private val TestFriendsPagingDataDto = FriendsPagingDataDto(
            pagingData = PagingData(
                pageNumber = 1,
                pageSize = 10,
                totalSize = 100,
            ),
            friends = listOf(TestFriendDto),
        )
    }
}
