package com.turnin.domain.account.presentation

import com.turnin.common.model.id.UserId
import com.turnin.common.route.Api
import com.turnin.domain.account.application.AccountUseCases
import com.turnin.domain.account.exception.AccountException
import com.turnin.util.testDeleteEndpoint
import com.turnin.util.testPlugin
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Test

class AccountRoutesTest {
    private val route = Api.V1.Account
    private val accountUseCases = mockk<AccountUseCases>()

    @Test
    fun `계정 삭제 호출 성공 테스트`() = testApplication {
        coEvery { accountUseCases.delete(any()) } returns Unit

        testDeleteEndpoint(
            endpoint = route.ROUTE,
            testPlugin = {
                testPlugin(
                    authRouting = { accountRoutes(route, accountUseCases) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
        )
    }

    @Test
    fun `계정 삭제 호출 성공 테스트 - 사용자가 존재하지 않는 경우 NotFound를 반환한다`() = testApplication {
        coEvery { accountUseCases.delete(any()) } throws AccountException.UserNotFound()

        testDeleteEndpoint(
            endpoint = route.ROUTE,
            testPlugin = {
                testPlugin(
                    authRouting = { accountRoutes(route, accountUseCases) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.NotFound,
        )
    }

    @Test
    fun `계정 삭제 호출 성공 테스트 - 사용토큰 없이 요청 시 401 에러를 반환한다`() = testApplication {
        coEvery { accountUseCases.delete(any()) } returns Unit

        testDeleteEndpoint(
            endpoint = route.ROUTE,
            testPlugin = {
                testPlugin(
                    authRouting = { accountRoutes(route, accountUseCases) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    companion object {
        private val TestUserId = UserId(1L)
    }
}
