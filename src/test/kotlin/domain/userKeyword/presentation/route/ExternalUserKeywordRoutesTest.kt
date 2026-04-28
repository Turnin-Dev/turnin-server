package com.turnin.domain.userKeyword.presentation.route

import com.turnin.common.exception.ApiException
import com.turnin.common.exception.common.CommonErrorCode
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.common.route.Api
import com.turnin.domain.userKeyword.application.dto.UserInfoDto
import com.turnin.domain.userKeyword.application.dto.UserKeywordDetailDto
import com.turnin.domain.userKeyword.application.usecase.UserKeywordUseCases
import com.turnin.domain.userKeyword.presentation.dto.toResponse
import com.turnin.util.testGetEndpoint
import com.turnin.util.testPlugin
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.serialization.json.Json

class ExternalUserKeywordRoutesTest {
    private val usecase: UserKeywordUseCases = mockk()
    private val route = Api.V1.User

    @Test
    fun `사용자 ID로 사용자 키워드 상세 정보 리스트 조회 - 요청 성공 테스트`() = testApplication {
        // given
        val expectedCount = 2
        val expectedList = List(expectedCount) { TestUserKeywordDetailDto }
        coEvery {
            usecase.getDetails(TestCurrentUserId.value, TestTargetUserId.value)
        } returns expectedList

        // when, then
        testGetEndpoint(
            endpoint = "${route.ROUTE}/${TestTargetUserId.value}/keywords",
            testPlugin = {
                testPlugin(
                    authRouting = { externalUserKeywordRoutes(route, usecase) },
                )
            },
            tokenSubject = TestCurrentUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
            responseValidator = {
                val expectedResponse = Json.encodeToString(expectedList.map { it.toResponse() })
                containsAll(expectedResponse)
            },
        )
    }

    @Test
    fun `사용자 ID로 사용자 키워드 상세 정보 리스트 조회 - 토큰 에러 발생 시 HTTP 상태코드 401을 반환한다`() = testApplication {
        testGetEndpoint(
            endpoint = "${route.ROUTE}/${TestTargetUserId.value}/keywords",
            testPlugin = {
                testPlugin(
                    authRouting = { externalUserKeywordRoutes(route, usecase) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    @Test
    fun `사용자 ID로 사용자 키워드 상세 정보 리스트 조회 - 알려진 예외가 발생하는 경우 정해진 메시지를 반환한다`() = testApplication {
        // given
        val expectedException = object : ApiException(
            errorCode = CommonErrorCode.MalformedRequest,
            status = HttpStatusCode.InternalServerError,
            message = "hello, error!",
        ) {}
        coEvery {
            usecase.getDetails(TestCurrentUserId.value, TestTargetUserId.value)
        } throws expectedException

        // when, then
        testGetEndpoint(
            endpoint = "${route.ROUTE}/${TestTargetUserId.value}/keywords",
            testPlugin = {
                testPlugin(
                    authRouting = { externalUserKeywordRoutes(route, usecase) },
                )
            },
            tokenSubject = TestCurrentUserId.value.toString(),
            expectedStatus = expectedException.status,
            responseValidator = {
                containsAll(
                    expectedException.errorCode.code,
                    expectedException.errorCode.description,
                )
            },
        )
    }

    companion object {
        private val TestCurrentUserId = UserId(1L)
        private val TestTargetUserId = UserId(2L)
        private val TestUserKeywordId = UserKeywordId(1L)
        private val TestUserKeywordDetailDto = UserKeywordDetailDto(
            userKeywordId = TestUserKeywordId.value,
            keywordId = 1L,
            keywordName = "keyword",
            description = "description",
            userInfo = UserInfoDto(
                userId = TestCurrentUserId.value,
                userName = "user",
                profileImageUrl = null,
            ),
            createdAt = 1000,
            updatedAt = 1000,
        )
    }
}
