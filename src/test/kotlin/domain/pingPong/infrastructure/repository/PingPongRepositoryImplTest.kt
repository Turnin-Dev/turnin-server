package com.turnin.domain.pingPong.infrastructure.repository

import com.turnin.common.db.DatabaseException
import com.turnin.common.db.schema.KeywordEntity
import com.turnin.common.db.schema.Keywords
import com.turnin.common.db.schema.PingPongAnswers
import com.turnin.common.db.schema.PingPongs
import com.turnin.common.db.schema.UserEntity
import com.turnin.common.db.schema.UserKeywordEntity
import com.turnin.common.db.schema.Users
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.id.PingPongId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.pingPong.domain.model.PingPongContent
import com.turnin.util.db.TestDatabaseFactory
import java.time.Instant
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.dao.id.EntityID
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

    private suspend fun insertUserAndReturnId(uniqueValue: String): UserId = TestDatabaseFactory.dbQuery {
        val savedUser = UserEntity.new {
            this.role = Role.USER
            this.provider = SocialLoginProvider.GOOGLE
            this.providerId = "pid$uniqueValue"
            this.displayId = "did$uniqueValue"
            this.name = "honggd"
            this.profileImageUrl = null
            this.introduce = "hello"
            this.isActive = true
            this.lastLoginAt = Instant.now()
        }

        UserId(savedUser.id.value)
    }

    private suspend fun insertUserKeywordAndReturnId(userId: Long): UserKeywordId = TestDatabaseFactory.dbQuery {
        val keywordId = KeywordEntity
            .new {
                this.keyword = "keyword"
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
}
