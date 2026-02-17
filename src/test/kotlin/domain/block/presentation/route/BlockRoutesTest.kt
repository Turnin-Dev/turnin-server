package com.peekr.domain.block.presentation.route

import com.peekr.common.exception.ApiException
import com.peekr.common.exception.common.CommonErrorCode
import com.peekr.common.model.UserName.Companion.invoke
import com.peekr.common.model.id.BlockId.Companion.invoke
import com.peekr.common.model.id.DisplayId.Companion.invoke
import com.peekr.common.model.id.UserId
import com.peekr.common.route.Api
import com.peekr.common.util.pagination.cursor.CursorPage
import com.peekr.domain.block.application.dto.BlockReasonDto
import com.peekr.domain.block.application.dto.BlockedUserDto
import com.peekr.domain.block.application.usecase.BlockUseCases
import com.peekr.domain.block.presentation.dto.BlockDetailRequest
import com.peekr.util.testGetEndpoint
import com.peekr.util.testPlugin
import com.peekr.util.testPostEndpoint
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlin.collections.List
import org.junit.Test

class BlockRoutesTest {
    private val route = Api.V1.Block
    private val usecase: BlockUseCases = mockk()

    @Test
    fun `차단 목록 조회 - 성공 테스트`() = testApplication {
        // given
        val cursorPage = CursorPage(
            items = List(3) {
                BlockedUserDto(
                    id = it + 1L,
                    userId = it + 1L,
                    displayId = "did${it + 1L}",
                    name = "name${it + 1L}",
                    profileImageUrl = "image${it + 1L}",
                )
            },
            nextCursor = 2L,
        )
        coEvery {
            usecase.getBlockedUsers(TestUserId.value, any(), any())
        } returns cursorPage

        // when, then
        testGetEndpoint(
            endpoint = route.ROUTE,
            queryParameters = mapOf(
                "page" to "1",
                "size" to "10",
            ),
            testPlugin = {
                testPlugin(
                    authRouting = { blockRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
            responseValidator = {
                containsAll(
                    cursorPage.nextCursor.toString(),
                    cursorPage.items.first().displayId,
                    cursorPage.items.first().name,
                    cursorPage.items.last().displayId,
                    cursorPage.items.last().name,
                )
            },
        )
    }

    @Test
    fun `차단 목록 조회 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        // given
        val expectedApiException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        coEvery {
            usecase.getBlockedUsers(TestUserId.value, any(), any())
        } throws expectedApiException

        // when, then
        testGetEndpoint(
            endpoint = route.ROUTE,
            queryParameters = mapOf(
                "page" to "1",
                "size" to "10",
            ),
            testPlugin = {
                testPlugin(
                    authRouting = { blockRoutes(route, usecase) },
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
    fun `차단 목록 조회 - 토큰 없이 요청 시 401 에러를 반환한다`() = testApplication {
        testGetEndpoint(
            endpoint = route.ROUTE,
            queryParameters = mapOf(
                "page" to "1",
                "size" to "10",
            ),
            testPlugin = {
                testPlugin(
                    authRouting = { blockRoutes(route, usecase) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    @Test
    fun `차단 사유 목록 조회 - 성공 테스트`() = testApplication {
        // given
        coEvery { usecase.getBlockReasons() } returns listOf(TestBlockReasonDto)

        // when, then
        testGetEndpoint(
            endpoint = "${route.ROUTE}/${route.REASON}",
            testPlugin = {
                testPlugin(
                    authRouting = { blockRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
            responseValidator = {
                containsAll(
                    TestBlockReasonDto.code,
                    TestBlockReasonDto.description,
                )
            },
        )
    }

    @Test
    fun `차단 생성 - 성공 테스트`() = testApplication {
        // given
        coEvery { usecase.createBlock(any()) } just Runs

        // when, then
        testPostEndpoint(
            endpoint = route.ROUTE,
            requestBody = TestBlockDetailRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { blockRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.Created,
        )
    }

    @Test
    fun `차단 생성 - 요청자의 ID와 blockerId가 일치하지 않는 경우 Forbidden을 반환한다`() = testApplication {
        // given
        coEvery { usecase.createBlock(any()) } just Runs

        // when, then
        testPostEndpoint(
            endpoint = route.ROUTE,
            requestBody = TestBlockDetailRequest.copy(blockerId = 100L),
            testPlugin = {
                testPlugin(
                    authRouting = { blockRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.Forbidden,
        )
    }

    @Test
    fun `차단 생성 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        // given
        val expectedApiException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        coEvery { usecase.createBlock(any()) } throws expectedApiException

        // when, then
        testPostEndpoint(
            endpoint = route.ROUTE,
            requestBody = TestBlockDetailRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { blockRoutes(route, usecase) },
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
    fun `차단 생성 - 토큰 없이 요청 시 401 에러를 반환한다`() = testApplication {
        testPostEndpoint(
            endpoint = route.ROUTE,
            requestBody = TestBlockDetailRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { blockRoutes(route, usecase) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    companion object {
        private val TestUserId = UserId(1L)
        private val TestBlockReasonDto = BlockReasonDto(
            id = 1L,
            code = "code",
            description = "description",
        )
        private val TestBlockDetailRequest = BlockDetailRequest(
            blockerId = TestUserId.value,
            blockedId = 2L,
            reasonId = 1L,
            customReason = "customReason",
        )
    }
}
