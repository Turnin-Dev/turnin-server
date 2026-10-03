package com.turnin.domain.pingPong.application.usecase

import com.turnin.common.model.UserName
import com.turnin.common.model.id.PingPongAnswerId
import com.turnin.common.model.id.PingPongId
import com.turnin.common.model.id.PingPongIdValidationException
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.pingPong.application.dto.PingPongAnswerDto
import com.turnin.domain.pingPong.application.dto.PingPongDetailDto
import com.turnin.domain.pingPong.application.dto.PingPongDto
import com.turnin.domain.pingPong.application.dto.PingPongQuestionerDto
import com.turnin.domain.pingPong.domain.model.PingPong
import com.turnin.domain.pingPong.domain.model.PingPongAnswer
import com.turnin.domain.pingPong.domain.model.PingPongContent
import com.turnin.domain.pingPong.domain.model.PingPongDetail
import com.turnin.domain.pingPong.domain.model.PingPongQuestioner
import com.turnin.domain.pingPong.domain.provider.UserKeywordProvider
import com.turnin.domain.pingPong.domain.repository.PingPongRepository
import com.turnin.domain.pingPong.exception.PingPongException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertThrows

class GetPingPongsUseCaseTest {
    private val pingPongRepository = mockk<PingPongRepository>()
    private val userKeywordProvider = mockk<UserKeywordProvider>()
    private val usecase = GetPingPongsUseCase(pingPongRepository, userKeywordProvider)

    @Test
    fun `게시물을 조회할 수 없으면 게시물 없음 예외가 발생하고 핑퐁 목록을 조회하지 않는다`() = runTest {
        // given
        coEvery { userKeywordProvider.findOwnerId(UserId(1L), UserKeywordId(3L)) } returns null

        // when, then
        assertThrows<PingPongException.UserKeywordNotFound> {
            usecase(UserId(1L), 3L, null, 10)
        }
        coVerify(exactly = 0) {
            pingPongRepository.findVisibleDetailsByUserKeywordId(UserId(1L), UserKeywordId(3L), null, any(), any())
        }
    }

    @Test
    fun `조회 결과가 페이지 크기보다 많으면 페이지 크기만큼 반환하고 마지막 핑퐁 ID를 다음 커서로 반환한다`() = runTest {
        // given
        coEvery { userKeywordProvider.findOwnerId(UserId(1L), UserKeywordId(3L)) } returns UserId(2L)
        coEvery {
            pingPongRepository.findVisibleDetailsByUserKeywordId(UserId(1L), UserKeywordId(3L), null, any(), any())
        } returns listOf(
            pingPongDetail(id = 30L),
            pingPongDetail(id = 20L),
            pingPongDetail(id = 10L),
        )

        // when
        val result = usecase(UserId(1L), 3L, null, 2)

        // then
        assertEquals(listOf(30L, 20L), result.items.map { it.pingPong.id })
        assertEquals(20L, result.nextCursor)
    }

    @Test
    fun `조회 결과가 페이지 크기 이하이면 전체를 매핑해 반환하고 다음 커서는 null이다`() = runTest {
        // given
        coEvery { userKeywordProvider.findOwnerId(UserId(1L), UserKeywordId(3L)) } returns UserId(2L)
        coEvery {
            pingPongRepository.findVisibleDetailsByUserKeywordId(UserId(1L), UserKeywordId(3L), null, any(), any())
        } returns listOf(
            PingPongDetail(
                pingPong = PingPong(
                    id = PingPongId(20L),
                    userKeywordId = UserKeywordId(3L),
                    questionerId = UserId(5L),
                    question = PingPongContent("두 번째 질문"),
                    createdAt = 2000L,
                    updatedAt = 2000L,
                ),
                questioner = PingPongQuestioner(
                    userId = UserId(5L),
                    userName = UserName("질문자"),
                    profileImageUrl = "https://example.com/profile.jpg",
                ),
                answer = null,
            ),
            PingPongDetail(
                pingPong = PingPong(
                    id = PingPongId(10L),
                    userKeywordId = UserKeywordId(3L),
                    questionerId = UserId(5L),
                    question = PingPongContent("첫 번째 질문"),
                    createdAt = 1000L,
                    updatedAt = 1000L,
                ),
                questioner = PingPongQuestioner(
                    userId = UserId(5L),
                    userName = UserName("질문자"),
                    profileImageUrl = "https://example.com/profile.jpg",
                ),
                answer = PingPongAnswer(
                    id = PingPongAnswerId(100L),
                    pingPongId = PingPongId(10L),
                    answer = PingPongContent("답변 내용"),
                    createdAt = 1500L,
                    updatedAt = 1500L,
                ),
            ),
        )

        // when
        val result = usecase(UserId(1L), 3L, null, 2)

        // then
        val expectedItems = listOf(
            PingPongDetailDto(
                pingPong = PingPongDto(
                    id = 20L,
                    userKeywordId = 3L,
                    questionerId = 5L,
                    question = "두 번째 질문",
                    createdAt = 2000L,
                    updatedAt = 2000L,
                ),
                questioner = PingPongQuestionerDto(
                    userId = 5L,
                    userName = "질문자",
                    profileImageUrl = "https://example.com/profile.jpg",
                ),
                answer = null,
            ),
            PingPongDetailDto(
                pingPong = PingPongDto(
                    id = 10L,
                    userKeywordId = 3L,
                    questionerId = 5L,
                    question = "첫 번째 질문",
                    createdAt = 1000L,
                    updatedAt = 1000L,
                ),
                questioner = PingPongQuestionerDto(
                    userId = 5L,
                    userName = "질문자",
                    profileImageUrl = "https://example.com/profile.jpg",
                ),
                answer = PingPongAnswerDto(
                    id = 100L,
                    pingPongId = 10L,
                    answer = "답변 내용",
                    createdAt = 1500L,
                    updatedAt = 1500L,
                ),
            ),
        )
        assertEquals(expectedItems, result.items)
        assertNull(result.nextCursor)
    }

    @Test
    fun `커서가 있으면 핑퐁 ID 커서와 페이지 크기보다 1 큰 개수로 조회한다`() = runTest {
        // given
        coEvery { userKeywordProvider.findOwnerId(UserId(1L), UserKeywordId(3L)) } returns UserId(2L)
        coEvery {
            pingPongRepository.findVisibleDetailsByUserKeywordId(
                UserId(1L),
                UserKeywordId(3L),
                PingPongId(50L),
                any(),
                any(),
            )
        } returns emptyList()

        // when
        usecase(UserId(1L), 3L, 50L, 10)

        // then
        coVerify(exactly = 1) {
            pingPongRepository.findVisibleDetailsByUserKeywordId(
                UserId(1L),
                UserKeywordId(3L),
                PingPongId(50L),
                11,
                any(),
            )
        }
    }

    @Test
    fun `커서가 없으면 커서 없이 조회한다`() = runTest {
        // given
        coEvery { userKeywordProvider.findOwnerId(UserId(1L), UserKeywordId(3L)) } returns UserId(2L)
        coEvery {
            pingPongRepository.findVisibleDetailsByUserKeywordId(UserId(1L), UserKeywordId(3L), null, any(), any())
        } returns emptyList()

        // when
        usecase(UserId(1L), 3L, null, 10)

        // then
        coVerify(exactly = 1) {
            pingPongRepository.findVisibleDetailsByUserKeywordId(UserId(1L), UserKeywordId(3L), null, any(), any())
        }
    }

    @Test
    fun `커서가 0 이하이면 유효성 검사 예외가 발생한다`() = runTest {
        // given
        coEvery { userKeywordProvider.findOwnerId(UserId(1L), UserKeywordId(3L)) } returns UserId(2L)
        val invalidCursor = 0L

        // when, then
        assertThrows<PingPongIdValidationException> {
            usecase(UserId(1L), 3L, invalidCursor, 10)
        }
    }

    @Test
    fun `게시물 작성자가 조회하면 차단 관계인 질문자의 핑퐁을 제외하지 않고 조회한다`() = runTest {
        // given
        coEvery { userKeywordProvider.findOwnerId(UserId(1L), UserKeywordId(3L)) } returns UserId(1L)
        coEvery {
            pingPongRepository.findVisibleDetailsByUserKeywordId(UserId(1L), UserKeywordId(3L), null, any(), any())
        } returns emptyList()

        // when
        usecase(UserId(1L), 3L, null, 10)

        // then
        coVerify(exactly = 1) {
            pingPongRepository.findVisibleDetailsByUserKeywordId(
                UserId(1L),
                UserKeywordId(3L),
                null,
                any(),
                excludeBlockedQuestioners = false,
            )
        }
    }

    @Test
    fun `게시물 작성자가 아닌 사용자가 조회하면 차단 관계인 질문자의 핑퐁을 제외하고 조회한다`() = runTest {
        // given
        coEvery { userKeywordProvider.findOwnerId(UserId(1L), UserKeywordId(3L)) } returns UserId(2L)
        coEvery {
            pingPongRepository.findVisibleDetailsByUserKeywordId(UserId(1L), UserKeywordId(3L), null, any(), any())
        } returns emptyList()

        // when
        usecase(UserId(1L), 3L, null, 10)

        // then
        coVerify(exactly = 1) {
            pingPongRepository.findVisibleDetailsByUserKeywordId(
                UserId(1L),
                UserKeywordId(3L),
                null,
                any(),
                excludeBlockedQuestioners = true,
            )
        }
    }

    private fun pingPongDetail(id: Long): PingPongDetail = PingPongDetail(
        pingPong = PingPong(
            id = PingPongId(id),
            userKeywordId = UserKeywordId(3L),
            questionerId = UserId(5L),
            question = PingPongContent("질문 내용"),
            createdAt = 1000L,
            updatedAt = 1000L,
        ),
        questioner = PingPongQuestioner(
            userId = UserId(5L),
            userName = UserName("질문자"),
            profileImageUrl = null,
        ),
        answer = null,
    )
}
