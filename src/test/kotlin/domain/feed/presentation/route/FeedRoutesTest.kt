package com.turnin.domain.feed.presentation.route

import com.turnin.common.model.id.UserId
import com.turnin.common.route.Api
import com.turnin.common.util.pagination.cursor.CursorPage
import com.turnin.common.util.pagination.cursor.toResponse
import com.turnin.domain.feed.application.dto.FeedDto
import com.turnin.domain.feed.application.usecase.FeedUseCases
import com.turnin.domain.feed.presentation.dto.FeedType
import com.turnin.domain.feed.presentation.dto.toResponse
import com.turnin.util.testGetEndpoint
import com.turnin.util.testPlugin
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.junit.Test

class FeedRoutesTest {
    private val route = Api.V1.Feed
    private val usecase: FeedUseCases = mockk()

    @Test
    fun `feed_type이 ALL이고 cursor가 없으면 allFeeds를 첫 페이지로 조회한다`() = testApplication {
        // given: cursor 없는 ALL 피드 첫 페이지
        val pageSize = 3
        val cursorPage = CursorPage<FeedDto, String>(
            items = createFeedDto(pageSize),
            nextCursor = null,
        )
        coEvery {
            usecase.allFeeds(TestUserId, null, pageSize)
        } returns cursorPage

        // when, then
        testGetEndpoint(
            endpoint = route.ROUTE,
            queryParameters = mapOf(
                "feed_type" to FeedType.ALL.name,
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
    fun `feed_type 파라미터를 생략하면 기본값 ALL로 처리한다`() = testApplication {
        // given: feed_type 쿼리 파라미터 생략
        val pageSize = 3
        val cursorPage = CursorPage<FeedDto, String>(
            items = createFeedDto(pageSize),
            nextCursor = null,
        )
        coEvery {
            usecase.allFeeds(TestUserId, null, pageSize)
        } returns cursorPage

        // when, then
        testGetEndpoint(
            endpoint = route.ROUTE,
            queryParameters = mapOf(
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

        // then: friendFeeds는 호출되지 않는다
        coVerify(exactly = 0) { usecase.friendFeeds(TestUserId, any(), any()) }
    }

    @Test
    fun `feed_type이 FRIEND이면 friendFeeds를 조회한다`() = testApplication {
        // given
        val pageSize = 3
        val cursorPage = CursorPage<FeedDto, String>(
            items = createFeedDto(pageSize),
            nextCursor = null,
        )
        coEvery {
            usecase.friendFeeds(TestUserId, null, pageSize)
        } returns cursorPage

        // when, then
        testGetEndpoint(
            endpoint = route.ROUTE,
            queryParameters = mapOf(
                "feed_type" to FeedType.FRIEND.name,
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

        // then: allFeeds는 호출되지 않는다
        coVerify(exactly = 0) { usecase.allFeeds(TestUserId, any(), any()) }
    }

    @Test
    fun `cursor 파라미터가 있으면 그대로 usecase에 전달한다`() = testApplication {
        // given: 이전 응답에서 받은 커서를 그대로 전달하는 상황을 재현
        val pageSize = 3
        val existingCursor = "eyJzZWVkIjoiYWJjIn0" // 임의의 인코딩된 커서 문자열
        val cursorPage = CursorPage<FeedDto, String>(
            items = createFeedDto(pageSize),
            nextCursor = "next-cursor-raw",
        )
        coEvery {
            usecase.allFeeds(TestUserId, existingCursor, pageSize)
        } returns cursorPage

        // when, then
        testGetEndpoint(
            endpoint = route.ROUTE,
            queryParameters = mapOf(
                "feed_type" to FeedType.ALL.name,
                "cursor" to existingCursor,
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
    fun `nextCursor가 있는 응답도 그대로 직렬화되어 내려온다`() = testApplication {
        // given
        val pageSize = 3
        val cursorPage = CursorPage<FeedDto, String>(
            items = createFeedDto(pageSize),
            nextCursor = "next-cursor-raw",
        )
        coEvery {
            usecase.allFeeds(TestUserId, null, pageSize)
        } returns cursorPage

        // when, then
        testGetEndpoint(
            endpoint = route.ROUTE,
            queryParameters = mapOf(
                "feed_type" to FeedType.ALL.name,
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
            )
        }

    companion object {
        private val TestUserId = UserId(1L)
    }
}
