package com.peekr.util

import com.peekr.common.jwt.domain.model.JWTToken
import com.peekr.domain.friend.presentation.dto.FriendsResponse
import com.peekr.util.TestClientFactory.createTestClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.server.testing.ApplicationTestBuilder
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

/**
 * 페이지네이션 테스트 유틸 함수
 *
 * 오프셋 기반 페이지네이션을 테스트한다.
 *
 * @param totalSize 기대하는 전체 데이터 개수
 * @param pageSize 한 페이지당 조회할 크기
 * @param fetcher 페이지네이션 조회를 수행하는 람다 함수
 */
fun <T> testPagination(
    totalSize: Int,
    pageSize: Int,
    fetcher: suspend (offset: Long, limit: Int) -> List<T>,
) = runTest {
    // ------------------------------ Given ------------------------------
    // 예상 페이지 개수
    val expectedPageCount = (totalSize + pageSize - 1) / pageSize
    var currentPage = 1L
    var offset: Long
    // 성공적으로 데이터가 조회된 횟수 (페이지 수)
    var actualFetchedCount = 0
    var totalFetchedItems = 0

    // ------------------------------ When, Then ------------------------------
    for (i in 0 until expectedPageCount) {
        // 1) offset 계산 (1-based page)
        offset = (currentPage - 1) * pageSize

        // 2) fetcher 람다 실행
        val items = fetcher(offset, pageSize)

        // 3) 예외
        if (items.isEmpty()) {
            fail("예상보다 일찍 빈 목록이 반환되었습니다. (조회 횟수: $actualFetchedCount)")
        }

        // 4) 검증: 마지막 페이지가 아니면 pageSize, 마지막 페이지면 나머지로 처리
        val isLastPage = (i == expectedPageCount - 1)
        val expectedItemCount =
            if (isLastPage && totalSize % pageSize != 0) {
                totalSize % pageSize
            } else {
                pageSize
            }
        assertEquals(expectedItemCount, items.size)
        totalFetchedItems += items.size

        // 5) 다음 페이지
        currentPage++
        actualFetchedCount++
    }

    // ------------------------------ Then: 최종 검증 ------------------------------
    val finalOffset = (currentPage - 1) * pageSize
    val finalItems = fetcher(finalOffset, pageSize)
    assertTrue(finalItems.isEmpty())
    assertEquals(expectedPageCount, actualFetchedCount)
    assertEquals(totalSize, totalFetchedItems)
}

/**
 * 페이지네이션 요청 및 응답 검증을 위한 헬퍼 함수
 *
 * @param testPlugin 테스트 플러그인 [testPlugin]
 * @param endpoint 테스트할 엔드포인트
 * @param queryParameters 쿼리 파라미터 (page, size 등)
 * @param token 테스트 인증 토큰
 * @param expectedSize 검증할 페이지 사이즈
 * @param expectedHasNext 검증할 다음 페이지 여부
 */
suspend fun ApplicationTestBuilder.testPaginationRoute(
    endpoint: String,
    queryParameters: Map<String, String>,
    token: JWTToken?,
    expectedSize: Int,
    expectedHasNext: Boolean,
) {
    val client = createTestClient()

    val response = client.get(endpoint) {
        url {
            queryParameters.forEach { (key, value) ->
                parameters.append(key, value)
            }
        }
        token?.let {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
        }
    }

    val responseBody = response.bodyAsText()
    val friendsResponse = Json.decodeFromString<FriendsResponse>(responseBody)

    // 검증
    assertEquals(expectedSize, friendsResponse.friends.size, "페이지 목록 크기 불일치")
    assertEquals(expectedHasNext, friendsResponse.hasNext, "페이지 hasNext 불일치")
}
