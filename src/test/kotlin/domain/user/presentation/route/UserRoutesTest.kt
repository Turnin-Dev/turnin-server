package com.peekr.domain.user.presentation.route

import com.peekr.common.api.Api
import com.peekr.common.exception.CommonErrorCode
import com.peekr.domain.user.UserTestDoubles.MockUserDto
import com.peekr.domain.user.application.usecase.UserUseCase
import com.peekr.util.TestClientFactory.createTestClient
import com.peekr.util.testPlugin
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test

class UserRoutesTest {
    private val route = Api.V1.User
    private val userUseCase = mockk<UserUseCase>()

    @Test
    fun `사용자 조회 GET 요청 성공 테스트`() = testApplication {
        // given
        val client = createTestClient()
        coEvery { userUseCase.getUserById(any()) } returns MockUserDto

        testPlugin(
            routing = { userRoutes(route, userUseCase) },
        )

        // when
        val getUserEndPoint = "${route.ROUTE}/1"
        val response = client.get(getUserEndPoint)
        val responseBody = response.bodyAsText()

        // then
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(responseBody.contains(MockUserDto.name))
        assertTrue(responseBody.contains(MockUserDto.displayId))
    }

    @Test
    fun `사용자 조회 GET 요청 실패 테스트 - 잘못된 형식의 사용자 ID인 경우`() = testApplication {
        // given
        val client = createTestClient()
        coEvery { userUseCase.getUserById(any()) } returns MockUserDto

        testPlugin(
            routing = { userRoutes(route, userUseCase) },
        )

        invalidUserIds.forEach { invalidUserId ->
            // when
            val getUserEndPoint = "${route.ROUTE}/$invalidUserId"
            val response = client.get(getUserEndPoint)
            val responseBody = response.bodyAsText()

            // then
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertTrue(responseBody.contains(CommonErrorCode.Validation.description))
        }
    }

    @Test
    fun `사용자 조회 GET 요청 실패 테스트 - 사용자가 존재하지 않는 경우`() = testApplication {
        // given
        val client = createTestClient()
        coEvery { userUseCase.getUserById(any()) } returns null

        testPlugin(
            routing = { userRoutes(route, userUseCase) },
        )

        // when
        val getUserEndPoint = "${route.ROUTE}/1"
        val response = client.get(getUserEndPoint)
        val responseBody = response.bodyAsText()

        // then
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(responseBody.contains("${HttpStatusCode.NotFound.value}"))
    }

    companion object {
        private const val NOT_NUMBER_USER_ID = "asd"
        private const val EMPTY_USER_ID = "asd"
        private val invalidUserIds = listOf(NOT_NUMBER_USER_ID, EMPTY_USER_ID)
    }
}
