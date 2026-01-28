package com.peekr.domain.feed.presentation.route

import com.peekr.common.model.id.UserId
import com.peekr.common.route.Api
import com.peekr.common.util.pagination.cursor.CursorPage
import com.peekr.common.util.pagination.cursor.toResponse
import com.peekr.domain.feed.application.dto.FeedCursor
import com.peekr.domain.feed.application.dto.FeedDto
import com.peekr.domain.feed.application.usecase.FeedUseCases
import com.peekr.domain.feed.presentation.dto.toResponse
import com.peekr.util.testGetEndpoint
import com.peekr.util.testPlugin
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.serialization.json.Json

class FeedRoutesTest {
    private val route = Api.V1.Feed
    private val usecase: FeedUseCases = mockk()

    @Test
    fun `nextCursor가 없는 피드 첫 페이지 조회`() = testApplication {
        // given: nextCursor가 없는 첫 페이지 데이터 준비
        val pageSize = 3
        val cursorPage = CursorPage<FeedDto, FeedCursor>(
            items = createFeedDto(pageSize),
            nextCursor = null,
        )
        coEvery {
            usecase.getFeeds(
                TestUserId.value,
                null,
                pageSize,
            )
        } returns cursorPage

        // when, then
        testGetEndpoint(
            endpoint = route.ROUTE,
            queryParameters = mapOf(
                "cursorScore" to "",
                "cursorCreatedAt" to "",
                "cursorUserKeywordId" to "",
                "size" to "$pageSize",
            ),
            testPlugin = {
                testPlugin(
                    authRouting = { feedRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
            responseValidator = {
                val body = Json.encodeToString(cursorPage.toResponse { it.toResponse() })
                contains(body)
            },
        )
    }

    @Test
    fun `nextCursor가 있는 피드 첫 페이지 조회`() = testApplication {
        // given: nextCursor가 있는 첫 페이지 데이터 준비
        val pageSize = 3
        val feedDtoList = createFeedDto(pageSize)
        val cursorPage = CursorPage(
            items = feedDtoList,
            nextCursor = FeedCursor(
                score = feedDtoList.last().score,
                createdAt = feedDtoList.last().createdAt,
                userKeywordId = feedDtoList.last().userKeywordId,
            ),
        )
        coEvery {
            usecase.getFeeds(
                TestUserId.value,
                null,
                pageSize,
            )
        } returns cursorPage

        // when, then
        testGetEndpoint(
            endpoint = route.ROUTE,
            queryParameters = mapOf(
                "cursorScore" to "",
                "cursorCreatedAt" to "",
                "cursorUserKeywordId" to "",
                "size" to "$pageSize",
            ),
            testPlugin = {
                testPlugin(
                    authRouting = { feedRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
            responseValidator = {
                val body = Json.encodeToString(cursorPage.toResponse { it.toResponse() })
                contains(body)
            },
        )
    }

    @Test
    fun `피드 중간 페이지 조회`() = testApplication {
        // given: 중간 페이지 데이터 준비
        val pageSize = 3
        val feedDtoList = createFeedDto(pageSize)
        val cursorPage = CursorPage(
            items = feedDtoList,
            nextCursor = FeedCursor(
                score = feedDtoList.last().score,
                createdAt = feedDtoList.last().createdAt,
                userKeywordId = feedDtoList.last().userKeywordId,
            ),
        )
        // 임의의 커서 값으로 모킹
        coEvery {
            usecase.getFeeds(
                TestUserId.value,
                FeedCursor(40.0, 1000L, 10L),
                pageSize,
            )
        } returns cursorPage

        // when: 위에서 모킹한 임의의 커서 값 그대로 호출
        // then
        testGetEndpoint(
            endpoint = route.ROUTE,
            queryParameters = mapOf(
                "cursorScore" to "40.0",
                "cursorCreatedAt" to "1000",
                "cursorUserKeywordId" to "10",
                "size" to "$pageSize",
            ),
            testPlugin = {
                testPlugin(
                    authRouting = { feedRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
            responseValidator = {
                val body = Json.encodeToString(cursorPage.toResponse { it.toResponse() })
                contains(body)
            },
        )
    }

    private fun createFeedDto(count: Int) =
        List(count) {
            val id = (it + 1).toLong()
            FeedDto(
                userKeywordId = id,
                userId = id,
                userName = "username$id",
                profileImageUrl = "profileImage$id",
                keywordId = id,
                keyword = "keyword$id",
                description = "description$id",
                createdAt = 1000L,
                score = 50.0,
                similarity = 0.8,
            )
        }

    companion object {
        private val TestUserId = UserId(1L)
    }
}
