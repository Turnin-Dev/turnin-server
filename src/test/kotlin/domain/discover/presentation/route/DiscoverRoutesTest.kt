package com.turnin.domain.discover.presentation.route

import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.common.route.Api
import com.turnin.common.util.pagination.cursor.CursorCodec
import com.turnin.common.util.pagination.cursor.CursorPage
import com.turnin.common.util.pagination.cursor.toResponse
import com.turnin.domain.discover.application.dto.DiscoverContextDto
import com.turnin.domain.discover.application.dto.DiscoverCursorDto
import com.turnin.domain.discover.application.dto.DiscoverKeywordDto
import com.turnin.domain.discover.application.dto.DiscoverUserDto
import com.turnin.domain.discover.application.usecase.DiscoverUseCases
import com.turnin.domain.discover.presentation.dto.toResponse
import com.turnin.util.testGetEndpoint
import com.turnin.util.testPlugin
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.serialization.json.Json

class DiscoverRoutesTest {
    private val route = Api.V1.Discover
    private val usecase: DiscoverUseCases = mockk()

    @Test
    fun `탐색 컨텍스트 목록 조회 - 페이지네이션 첫 페이지 조회`() = testApplication {
        val pageSize = 10
        val testCursorPage = CursorPage<DiscoverContextDto, String>(
            items = List(pageSize) { pageIndex ->
                val pageNumber = pageIndex + 1L
                DiscoverContextDto(
                    user = DiscoverUserDto(
                        id = UserId(pageNumber),
                        name = "$pageNumber",
                        displayId = "did$pageNumber",
                        profileImageUrl = "image",
                    ),
                    keywords = listOf(
                        DiscoverKeywordDto(
                            userKeywordId = UserKeywordId(pageNumber),
                            keywordId = KeywordId(pageNumber),
                            keywordName = "keyword",
                        ),
                    ),
                )
            },
            nextCursor = null,
        )
        coEvery {
            usecase.getDiscoverContext(
                targetUserId = TestUserId.value,
                viewerUserId = TestUserId.value,
                cursorRaw = null,
                pageSize = pageSize,
            )
        } returns testCursorPage

        testGetEndpoint(
            endpoint = route.ROUTE,
            queryParameters = mapOf(
                "userId" to TestUserId.value.toString(),
                "cursor" to "",
                "size" to "$pageSize",
            ),
            testPlugin = {
                testPlugin(
                    authRouting = { discoverRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
            responseValidator = {
                val body = Json.encodeToString(testCursorPage.toResponse { it.toResponse() })
                contains(body)
            },
        )
    }

    @Test
    fun `탐색 컨텍스트 목록 조회 - 페이지네이션 중간 페이지 조회`() = testApplication {
        val pageSize = 10
        val cursorDto = DiscoverCursorDto(
            seed = "test-seed",
            lastScore = 0.87,
            lastShuffleKey = 42,
            lastUserId = 3L,
        )
        val cursorRaw = CursorCodec.encode(cursorDto)

        val testCursorPage = CursorPage<DiscoverContextDto, String>(
            items = List(pageSize) { pageIndex ->
                val pageNumber = pageIndex + 1L
                DiscoverContextDto(
                    user = DiscoverUserDto(
                        id = UserId(pageNumber),
                        name = "$pageNumber",
                        displayId = "did$pageNumber",
                        profileImageUrl = "image",
                    ),
                    keywords = listOf(
                        DiscoverKeywordDto(
                            userKeywordId = UserKeywordId(pageNumber),
                            keywordId = KeywordId(pageNumber),
                            keywordName = "keyword",
                        ),
                    ),
                )
            },
            nextCursor = null,
        )
        coEvery {
            usecase.getDiscoverContext(
                targetUserId = TestUserId.value,
                viewerUserId = TestUserId.value,
                cursorRaw = cursorRaw,
                pageSize = pageSize,
            )
        } returns testCursorPage

        testGetEndpoint(
            endpoint = route.ROUTE,
            queryParameters = mapOf(
                "userId" to TestUserId.value.toString(),
                "cursor" to cursorRaw,
                "size" to "$pageSize",
            ),
            testPlugin = {
                testPlugin(
                    authRouting = { discoverRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
            responseValidator = {
                val body = Json.encodeToString(testCursorPage.toResponse { it.toResponse() })
                contains(body)
            },
        )
    }

    companion object {
        private val TestUserId = UserId(1L)
    }
}
