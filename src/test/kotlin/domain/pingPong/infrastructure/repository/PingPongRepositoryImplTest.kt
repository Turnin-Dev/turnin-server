package com.turnin.domain.pingPong.infrastructure.repository

import com.turnin.common.db.DatabaseException
import com.turnin.common.db.schema.BlockReasons
import com.turnin.common.db.schema.Blocks
import com.turnin.common.db.schema.KeywordEntity
import com.turnin.common.db.schema.Keywords
import com.turnin.common.db.schema.PingPongAnswers
import com.turnin.common.db.schema.PingPongs
import com.turnin.common.db.schema.UserEntity
import com.turnin.common.db.schema.UserKeywordEntity
import com.turnin.common.db.schema.Users
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.id.PingPongAnswerId
import com.turnin.common.model.id.PingPongId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.pingPong.domain.model.PingPongContent
import com.turnin.util.db.TestDatabaseFactory
import com.turnin.util.db.setUserInactiveForTest
import java.time.Instant
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class PingPongRepositoryImplTest {
    private val repository = PingPongRepositoryImpl()

    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @After
    fun teardown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `핑퐁 생성 시 입력한 값으로 생성된 핑퐁을 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)

        // when
        val result = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))

        // then
        assertEquals(userKeywordId, result.userKeywordId)
        assertEquals(questionerId, result.questionerId)
        assertEquals("질문 내용", result.question.value)
    }

    @Test
    fun `핑퐁 생성 시 숨김 처리되지 않은 상태로 저장된다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)

        // when
        val result = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))

        // then
        val savedRows = TestDatabaseFactory.dbQuery {
            PingPongs
                .selectAll()
                .where { PingPongs.id eq result.id.value }
                .map { it[PingPongs.question] to it[PingPongs.questionHiddenAt] }
        }
        assertEquals(1, savedRows.size)
        assertEquals("질문 내용", savedRows.first().first)
        assertNull(savedRows.first().second)
    }

    @Test
    fun `같은 사용자가 같은 게시물에 여러 번 질문해도 각각 저장된다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        repository.create(userKeywordId, questionerId, PingPongContent("첫 번째 질문"))

        // when
        repository.create(userKeywordId, questionerId, PingPongContent("두 번째 질문"))

        // then
        val count = TestDatabaseFactory.dbQuery {
            PingPongs
                .selectAll()
                .where { PingPongs.userKeywordId eq userKeywordId.value }
                .count()
        }
        assertEquals(2, count)
    }

    @Test
    fun `존재하지 않는 게시물에 핑퐁 생성 시 외래키 제약 위반 예외가 발생한다`() = runTest {
        // given
        val questionerId = insertUserAndReturnId("1")
        val notExistsUserKeywordId = UserKeywordId(999L)

        // when, then
        assertThrows<DatabaseException.ForeignKeyViolationException> {
            repository.create(notExistsUserKeywordId, questionerId, PingPongContent("질문 내용"))
        }
    }

    @Test
    fun `핑퐁 조회 시 핑퐁이 있으면 해당 핑퐁을 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val pingPong = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))

        // when
        val result = repository.findVisibleById(pingPong.id)

        // then
        assertNotNull(result)
        assertEquals(pingPong.id, result.id)
        assertEquals(userKeywordId, result.userKeywordId)
        assertEquals(questionerId, result.questionerId)
        assertEquals("질문 내용", result.question.value)
    }

    @Test
    fun `핑퐁 조회 시 핑퐁이 없으면 null을 반환한다`() = runTest {
        // given
        val notExistsPingPongId = PingPongId(999L)

        // when
        val result = repository.findVisibleById(notExistsPingPongId)

        // then
        assertNull(result)
    }

    @Test
    fun `핑퐁 조회 시 숨김 처리된 핑퐁이면 null을 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val pingPong = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))
        TestDatabaseFactory.dbQuery {
            PingPongs.update({ PingPongs.id eq pingPong.id.value }) {
                it[questionHiddenAt] = OffsetDateTime.parse("2026-01-01T00:00:00Z")
            }
        }

        // when
        val result = repository.findVisibleById(pingPong.id)

        // then
        assertNull(result)
    }

    @Test
    fun `답변 생성 시 입력한 값으로 생성된 답변을 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val pingPong = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))

        // when
        val result = repository.createAnswer(pingPong.id, PingPongContent("답변 내용"))

        // then
        assertEquals(pingPong.id, result.pingPongId)
        assertEquals("답변 내용", result.answer.value)
    }

    @Test
    fun `답변 생성 시 숨김 처리되지 않은 상태로 저장된다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val pingPong = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))

        // when
        val result = repository.createAnswer(pingPong.id, PingPongContent("답변 내용"))

        // then
        val savedRows = TestDatabaseFactory.dbQuery {
            PingPongAnswers
                .selectAll()
                .where { PingPongAnswers.id eq result.id.value }
                .map { it[PingPongAnswers.answer] to it[PingPongAnswers.hiddenAt] }
        }
        assertEquals(1, savedRows.size)
        assertEquals("답변 내용", savedRows.first().first)
        assertNull(savedRows.first().second)
    }

    @Test
    fun `이미 답변이 등록된 핑퐁에 답변 생성 시 중복 데이터 예외가 발생한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val pingPong = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))
        repository.createAnswer(pingPong.id, PingPongContent("첫 번째 답변"))

        // when, then
        assertThrows<DatabaseException.DuplicatedDataException> {
            repository.createAnswer(pingPong.id, PingPongContent("두 번째 답변"))
        }
    }

    @Test
    fun `존재하지 않는 핑퐁에 답변 생성 시 외래키 제약 위반 예외가 발생한다`() = runTest {
        // given
        val notExistsPingPongId = PingPongId(999L)

        // when, then
        assertThrows<DatabaseException.ForeignKeyViolationException> {
            repository.createAnswer(notExistsPingPongId, PingPongContent("답변 내용"))
        }
    }

    @Test
    fun `핑퐁 목록 조회 시 게시물의 핑퐁을 최신순으로 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val first = repository.create(userKeywordId, questionerId, PingPongContent("첫 번째 질문"))
        val second = repository.create(userKeywordId, questionerId, PingPongContent("두 번째 질문"))

        // when
        val result = repository.findVisibleDetailsByUserKeywordId(
            ownerId,
            userKeywordId,
            null,
            10,
            excludeBlockedQuestioners = true,
        )

        // then
        assertEquals(listOf(second.id, first.id), result.map { it.pingPong.id })
        assertEquals(listOf("두 번째 질문", "첫 번째 질문"), result.map { it.pingPong.question.value })
    }

    @Test
    fun `핑퐁 목록 조회 시 다른 게시물의 핑퐁은 반환하지 않는다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value, keyword = "keyword1")
        val otherUserKeywordId = insertUserKeywordAndReturnId(ownerId.value, keyword = "keyword2")
        val pingPong = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))
        repository.create(otherUserKeywordId, questionerId, PingPongContent("다른 게시물 질문"))

        // when
        val result = repository.findVisibleDetailsByUserKeywordId(
            ownerId,
            userKeywordId,
            null,
            10,
            excludeBlockedQuestioners = true,
        )

        // then
        assertEquals(listOf(pingPong.id), result.map { it.pingPong.id })
    }

    @Test
    fun `핑퐁 목록 조회 시 숨김 처리된 질문은 반환하지 않는다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val visible = repository.create(userKeywordId, questionerId, PingPongContent("노출 질문"))
        val hidden = repository.create(userKeywordId, questionerId, PingPongContent("숨김 질문"))
        TestDatabaseFactory.dbQuery {
            PingPongs.update({ PingPongs.id eq hidden.id.value }) {
                it[questionHiddenAt] = OffsetDateTime.parse("2026-01-01T00:00:00Z")
            }
        }

        // when
        val result = repository.findVisibleDetailsByUserKeywordId(
            ownerId,
            userKeywordId,
            null,
            10,
            excludeBlockedQuestioners = true,
        )

        // then
        assertEquals(listOf(visible.id), result.map { it.pingPong.id })
    }

    @Test
    fun `핑퐁 목록 조회 시 비활성화된 질문자의 핑퐁은 반환하지 않는다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val activeQuestionerId = insertUserAndReturnId("2")
        val inactiveQuestionerId = insertUserAndReturnId("3")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val visible = repository.create(userKeywordId, activeQuestionerId, PingPongContent("활성 사용자 질문"))
        repository.create(userKeywordId, inactiveQuestionerId, PingPongContent("비활성 사용자 질문"))
        setUserInactiveForTest(inactiveQuestionerId)

        // when
        val result = repository.findVisibleDetailsByUserKeywordId(
            ownerId,
            userKeywordId,
            null,
            10,
            excludeBlockedQuestioners = true,
        )

        // then
        assertEquals(listOf(visible.id), result.map { it.pingPong.id })
    }

    @Test
    fun `핑퐁 목록 조회 시 조회자가 차단한 질문자의 핑퐁은 반환하지 않는다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val viewerId = insertUserAndReturnId("2")
        val questionerId = insertUserAndReturnId("3")
        val blockedQuestionerId = insertUserAndReturnId("4")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val visible = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))
        repository.create(userKeywordId, blockedQuestionerId, PingPongContent("차단된 사용자 질문"))
        insertBlock(blockerId = viewerId, blockedId = blockedQuestionerId)

        // when
        val result = repository.findVisibleDetailsByUserKeywordId(
            viewerId,
            userKeywordId,
            null,
            10,
            excludeBlockedQuestioners = true,
        )

        // then
        assertEquals(listOf(visible.id), result.map { it.pingPong.id })
    }

    @Test
    fun `핑퐁 목록 조회 시 조회자를 차단한 질문자의 핑퐁은 반환하지 않는다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val viewerId = insertUserAndReturnId("2")
        val questionerId = insertUserAndReturnId("3")
        val blockingQuestionerId = insertUserAndReturnId("4")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val visible = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))
        repository.create(userKeywordId, blockingQuestionerId, PingPongContent("차단한 사용자 질문"))
        insertBlock(blockerId = blockingQuestionerId, blockedId = viewerId)

        // when
        val result = repository.findVisibleDetailsByUserKeywordId(
            viewerId,
            userKeywordId,
            null,
            10,
            excludeBlockedQuestioners = true,
        )

        // then
        assertEquals(listOf(visible.id), result.map { it.pingPong.id })
    }

    @Test
    fun `핑퐁 목록 조회 시 차단 필터를 끄면 조회자가 차단한 질문자의 핑퐁도 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val blockedQuestionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val pingPong = repository.create(userKeywordId, blockedQuestionerId, PingPongContent("차단된 사용자 질문"))
        insertBlock(blockerId = ownerId, blockedId = blockedQuestionerId)

        // when
        val result = repository.findVisibleDetailsByUserKeywordId(
            ownerId,
            userKeywordId,
            null,
            10,
            excludeBlockedQuestioners = false,
        )

        // then
        assertEquals(listOf(pingPong.id), result.map { it.pingPong.id })
    }

    @Test
    fun `핑퐁 목록 조회 시 차단 필터를 끄면 조회자를 차단한 질문자의 핑퐁도 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val blockingQuestionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val pingPong = repository.create(userKeywordId, blockingQuestionerId, PingPongContent("차단한 사용자 질문"))
        insertBlock(blockerId = blockingQuestionerId, blockedId = ownerId)

        // when
        val result = repository.findVisibleDetailsByUserKeywordId(
            ownerId,
            userKeywordId,
            null,
            10,
            excludeBlockedQuestioners = false,
        )

        // then
        assertEquals(listOf(pingPong.id), result.map { it.pingPong.id })
    }

    @Test
    fun `핑퐁 목록 조회 시 조회자 본인이 작성한 핑퐁을 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val viewerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val myPingPong = repository.create(userKeywordId, viewerId, PingPongContent("내 질문"))

        // when
        val result = repository.findVisibleDetailsByUserKeywordId(
            viewerId,
            userKeywordId,
            null,
            10,
            excludeBlockedQuestioners = true,
        )

        // then
        assertEquals(listOf(myPingPong.id), result.map { it.pingPong.id })
    }

    @Test
    fun `핑퐁 목록 조회 시 질문자 정보를 함께 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId(
            uniqueValue = "2",
            name = "질문자",
            profileImageUrl = "https://example.com/profile.jpg",
        )
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))

        // when
        val result = repository.findVisibleDetailsByUserKeywordId(
            ownerId,
            userKeywordId,
            null,
            10,
            excludeBlockedQuestioners = true,
        )

        // then
        val questioner = result.single().questioner
        assertEquals(questionerId, questioner.userId)
        assertEquals("질문자", questioner.userName.value)
        assertEquals("https://example.com/profile.jpg", questioner.profileImageUrl)
    }

    @Test
    fun `핑퐁 목록 조회 시 답변이 있으면 답변을 함께 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val pingPong = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))
        val answer = repository.createAnswer(pingPong.id, PingPongContent("답변 내용"))

        // when
        val result = repository.findVisibleDetailsByUserKeywordId(
            ownerId,
            userKeywordId,
            null,
            10,
            excludeBlockedQuestioners = true,
        )

        // then
        val resultAnswer = result.single().answer
        assertNotNull(resultAnswer)
        assertEquals(answer.id, resultAnswer.id)
        assertEquals(pingPong.id, resultAnswer.pingPongId)
        assertEquals("답변 내용", resultAnswer.answer.value)
    }

    @Test
    fun `핑퐁 목록 조회 시 답변이 없으면 답변을 null로 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))

        // when
        val result = repository.findVisibleDetailsByUserKeywordId(
            ownerId,
            userKeywordId,
            null,
            10,
            excludeBlockedQuestioners = true,
        )

        // then
        assertNull(result.single().answer)
    }

    @Test
    fun `핑퐁 목록 조회 시 답변이 숨김 처리되었으면 질문은 반환하고 답변은 null로 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val pingPong = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))
        val answer = repository.createAnswer(pingPong.id, PingPongContent("답변 내용"))
        TestDatabaseFactory.dbQuery {
            PingPongAnswers.update({ PingPongAnswers.id eq answer.id.value }) {
                it[hiddenAt] = OffsetDateTime.parse("2026-01-01T00:00:00Z")
            }
        }

        // when
        val result = repository.findVisibleDetailsByUserKeywordId(
            ownerId,
            userKeywordId,
            null,
            10,
            excludeBlockedQuestioners = true,
        )

        // then
        assertEquals(pingPong.id, result.single().pingPong.id)
        assertNull(result.single().answer)
    }

    @Test
    fun `핑퐁 목록 조회 시 커서가 있으면 커서보다 작은 ID의 핑퐁만 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val first = repository.create(userKeywordId, questionerId, PingPongContent("첫 번째 질문"))
        val second = repository.create(userKeywordId, questionerId, PingPongContent("두 번째 질문"))
        repository.create(userKeywordId, questionerId, PingPongContent("세 번째 질문"))

        // when
        val result = repository.findVisibleDetailsByUserKeywordId(
            ownerId,
            userKeywordId,
            second.id,
            10,
            excludeBlockedQuestioners = true,
        )

        // then
        assertEquals(listOf(first.id), result.map { it.pingPong.id })
    }

    @Test
    fun `핑퐁 목록 조회 시 조회 개수만큼만 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        repository.create(userKeywordId, questionerId, PingPongContent("첫 번째 질문"))
        val second = repository.create(userKeywordId, questionerId, PingPongContent("두 번째 질문"))
        val third = repository.create(userKeywordId, questionerId, PingPongContent("세 번째 질문"))

        // when
        val result = repository.findVisibleDetailsByUserKeywordId(
            ownerId,
            userKeywordId,
            null,
            2,
            excludeBlockedQuestioners = true,
        )

        // then
        assertEquals(listOf(third.id, second.id), result.map { it.pingPong.id })
    }

    @Test
    fun `핑퐁 목록 조회 시 핑퐁이 없으면 빈 목록을 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)

        // when
        val result = repository.findVisibleDetailsByUserKeywordId(
            ownerId,
            userKeywordId,
            null,
            10,
            excludeBlockedQuestioners = true,
        )

        // then
        assertEquals(emptyList(), result)
    }

    @Test
    fun `질문별 답변 조회 시 답변이 있으면 해당 답변을 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val pingPong = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))
        val answer = repository.createAnswer(pingPong.id, PingPongContent("답변 내용"))

        // when
        val result = repository.findVisibleAnswerByPingPongId(pingPong.id)

        // then
        assertNotNull(result)
        assertEquals(answer.id, result.id)
        assertEquals(pingPong.id, result.pingPongId)
        assertEquals("답변 내용", result.answer.value)
    }

    @Test
    fun `질문별 답변 조회 시 답변이 없으면 null을 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val pingPong = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))

        // when
        val result = repository.findVisibleAnswerByPingPongId(pingPong.id)

        // then
        assertNull(result)
    }

    @Test
    fun `질문별 답변 조회 시 숨김 처리된 답변이면 null을 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val pingPong = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))
        val answer = repository.createAnswer(pingPong.id, PingPongContent("답변 내용"))
        setAnswerHiddenAt(answer.id, OffsetDateTime.parse("2026-01-01T00:00:00Z"))

        // when
        val result = repository.findVisibleAnswerByPingPongId(pingPong.id)

        // then
        assertNull(result)
    }

    @Test
    fun `답변자 정보와 함께 답변 조회 시 게시물 작성자를 답변자로 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val pingPong = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))
        val answer = repository.createAnswer(pingPong.id, PingPongContent("답변 내용"))

        // when
        val result = repository.findVisibleAnswerWithAnswererById(answer.id)

        // then
        assertNotNull(result)
        assertEquals(ownerId, result.answererId)
        assertEquals(answer.id, result.answer.id)
        assertEquals(pingPong.id, result.answer.pingPongId)
        assertEquals("답변 내용", result.answer.answer.value)
    }

    @Test
    fun `답변자 정보와 함께 답변 조회 시 숨김 처리된 답변이면 null을 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val pingPong = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))
        val answer = repository.createAnswer(pingPong.id, PingPongContent("답변 내용"))
        setAnswerHiddenAt(answer.id, OffsetDateTime.parse("2026-01-01T00:00:00Z"))

        // when
        val result = repository.findVisibleAnswerWithAnswererById(answer.id)

        // then
        assertNull(result)
    }

    @Test
    fun `답변자 정보와 함께 답변 조회 시 답변이 달린 질문이 숨김 처리되었으면 null을 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val pingPong = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))
        val answer = repository.createAnswer(pingPong.id, PingPongContent("답변 내용"))
        setQuestionHiddenAt(pingPong.id, OffsetDateTime.parse("2026-01-01T00:00:00Z"))

        // when
        val result = repository.findVisibleAnswerWithAnswererById(answer.id)

        // then
        assertNull(result)
    }

    @Test
    fun `답변자 정보와 함께 답변 조회 시 답변이 없으면 null을 반환한다`() = runTest {
        // given
        val notExistsAnswerId = PingPongAnswerId(999L)

        // when
        val result = repository.findVisibleAnswerWithAnswererById(notExistsAnswerId)

        // then
        assertNull(result)
    }

    @Test
    fun `핑퐁 삭제 시 핑퐁과 연결된 답변이 함께 삭제되고 true를 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val pingPong = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))
        repository.createAnswer(pingPong.id, PingPongContent("답변 내용"))

        // when
        val result = repository.delete(pingPong.id)

        // then
        assertTrue(result)
        val (pingPongCount, answerCount) = TestDatabaseFactory.dbQuery {
            PingPongs.selectAll().where { PingPongs.id eq pingPong.id.value }.count() to
                PingPongAnswers.selectAll().where { PingPongAnswers.pingPongId eq pingPong.id.value }.count()
        }
        assertEquals(0, pingPongCount)
        assertEquals(0, answerCount)
    }

    @Test
    fun `핑퐁 삭제 시 핑퐁이 없으면 false를 반환한다`() = runTest {
        // given
        val notExistsPingPongId = PingPongId(999L)

        // when
        val result = repository.delete(notExistsPingPongId)

        // then
        assertFalse(result)
    }

    @Test
    fun `답변 삭제 시 답변만 삭제되고 질문은 유지되며 true를 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val pingPong = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))
        val answer = repository.createAnswer(pingPong.id, PingPongContent("답변 내용"))

        // when
        val result = repository.deleteAnswer(answer.id)

        // then
        assertTrue(result)
        val (pingPongCount, answerCount) = TestDatabaseFactory.dbQuery {
            PingPongs.selectAll().where { PingPongs.id eq pingPong.id.value }.count() to
                PingPongAnswers.selectAll().where { PingPongAnswers.id eq answer.id.value }.count()
        }
        assertEquals(1, pingPongCount)
        assertEquals(0, answerCount)
    }

    @Test
    fun `답변 삭제 시 답변이 없으면 false를 반환한다`() = runTest {
        // given
        val notExistsAnswerId = PingPongAnswerId(999L)

        // when
        val result = repository.deleteAnswer(notExistsAnswerId)

        // then
        assertFalse(result)
    }

    @Test
    fun `핑퐁 숨김 처리 시 숨김 시각을 기록하고 true를 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val pingPong = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))

        // when
        val result = repository.hide(pingPong.id)

        // then
        assertTrue(result)
        val hiddenAt = TestDatabaseFactory.dbQuery {
            PingPongs.selectAll().where { PingPongs.id eq pingPong.id.value }.single()[PingPongs.questionHiddenAt]
        }
        assertNotNull(hiddenAt)
    }

    @Test
    fun `핑퐁 숨김 처리 시 이미 숨김 처리된 핑퐁이면 숨김 시각을 유지하고 false를 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val pingPong = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))
        setQuestionHiddenAt(pingPong.id, OffsetDateTime.parse("2026-01-01T00:00:00Z"))

        // when
        val result = repository.hide(pingPong.id)

        // then
        assertFalse(result)
        val hiddenAt = TestDatabaseFactory.dbQuery {
            PingPongs.selectAll().where { PingPongs.id eq pingPong.id.value }.single()[PingPongs.questionHiddenAt]
        }
        assertEquals(OffsetDateTime.parse("2026-01-01T00:00:00Z").toInstant(), hiddenAt?.toInstant())
    }

    @Test
    fun `핑퐁 숨김 처리 시 핑퐁이 없으면 false를 반환한다`() = runTest {
        // given
        val notExistsPingPongId = PingPongId(999L)

        // when
        val result = repository.hide(notExistsPingPongId)

        // then
        assertFalse(result)
    }

    @Test
    fun `답변 숨김 처리 시 숨김 시각을 기록하고 true를 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val pingPong = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))
        val answer = repository.createAnswer(pingPong.id, PingPongContent("답변 내용"))

        // when
        val result = repository.hideAnswer(answer.id)

        // then
        assertTrue(result)
        val hiddenAt = TestDatabaseFactory.dbQuery {
            PingPongAnswers
                .selectAll()
                .where { PingPongAnswers.id eq answer.id.value }
                .single()[PingPongAnswers.hiddenAt]
        }
        assertNotNull(hiddenAt)
    }

    @Test
    fun `답변 숨김 처리 시 이미 숨김 처리된 답변이면 숨김 시각을 유지하고 false를 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val questionerId = insertUserAndReturnId("2")
        val userKeywordId = insertUserKeywordAndReturnId(ownerId.value)
        val pingPong = repository.create(userKeywordId, questionerId, PingPongContent("질문 내용"))
        val answer = repository.createAnswer(pingPong.id, PingPongContent("답변 내용"))
        setAnswerHiddenAt(answer.id, OffsetDateTime.parse("2026-01-01T00:00:00Z"))

        // when
        val result = repository.hideAnswer(answer.id)

        // then
        assertFalse(result)
        val hiddenAt = TestDatabaseFactory.dbQuery {
            PingPongAnswers
                .selectAll()
                .where { PingPongAnswers.id eq answer.id.value }
                .single()[PingPongAnswers.hiddenAt]
        }
        assertEquals(OffsetDateTime.parse("2026-01-01T00:00:00Z").toInstant(), hiddenAt?.toInstant())
    }

    @Test
    fun `답변 숨김 처리 시 답변이 없으면 false를 반환한다`() = runTest {
        // given
        val notExistsAnswerId = PingPongAnswerId(999L)

        // when
        val result = repository.hideAnswer(notExistsAnswerId)

        // then
        assertFalse(result)
    }

    private suspend fun setQuestionHiddenAt(pingPongId: PingPongId, hiddenAt: OffsetDateTime) =
        TestDatabaseFactory.dbQuery {
            PingPongs.update({ PingPongs.id eq pingPongId.value }) {
                it[questionHiddenAt] = hiddenAt
            }
        }

    private suspend fun setAnswerHiddenAt(pingPongAnswerId: PingPongAnswerId, hiddenAt: OffsetDateTime) =
        TestDatabaseFactory.dbQuery {
            PingPongAnswers.update({ PingPongAnswers.id eq pingPongAnswerId.value }) {
                it[PingPongAnswers.hiddenAt] = hiddenAt
            }
        }

    private suspend fun insertUserAndReturnId(
        uniqueValue: String,
        name: String = "honggd",
        profileImageUrl: String? = null,
    ): UserId = TestDatabaseFactory.dbQuery {
        val savedUser = UserEntity.new {
            this.role = Role.USER
            this.provider = SocialLoginProvider.GOOGLE
            this.providerId = "pid$uniqueValue"
            this.displayId = "did$uniqueValue"
            this.name = name
            this.profileImageUrl = profileImageUrl
            this.introduce = "hello"
            this.isActive = true
            this.lastLoginAt = Instant.now()
        }

        UserId(savedUser.id.value)
    }

    private suspend fun insertUserKeywordAndReturnId(
        userId: Long,
        keyword: String = "keyword",
    ): UserKeywordId = TestDatabaseFactory.dbQuery {
        val keywordId = KeywordEntity
            .new {
                this.keyword = keyword
                this.embedding = "embedding"
                this.createdBy = EntityID(userId, Users)
            }.id.value

        val savedUserKeyword = UserKeywordEntity.new {
            this.userId = EntityID(userId, Users)
            this.keywordId = EntityID(keywordId, Keywords)
            this.description = "description"
        }

        UserKeywordId(savedUserKeyword.id.value)
    }

    private suspend fun insertBlock(blockerId: UserId, blockedId: UserId) = TestDatabaseFactory.dbQuery {
        Blocks.insert {
            it[Blocks.blockerId] = EntityID(blockerId.value, Users)
            it[Blocks.blockedId] = EntityID(blockedId.value, Users)
            it[Blocks.reasonId] = EntityID(1L, BlockReasons) // 기존 initData에서 생성된 차단 사유
        }
    }
}
