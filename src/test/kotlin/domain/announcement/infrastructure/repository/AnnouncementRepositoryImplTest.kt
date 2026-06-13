package com.turnin.domain.announcement.infrastructure.repository

import com.turnin.common.db.schema.AnnouncementEntity
import com.turnin.common.db.schema.AnnouncementReads
import com.turnin.common.db.schema.UserEntity
import com.turnin.common.model.AnnouncementAudience
import com.turnin.common.model.AnnouncementStatus
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.id.AnnouncementId
import com.turnin.common.model.id.UserId
import com.turnin.common.util.TurninDateTime
import com.turnin.common.util.toOffsetDateTime
import com.turnin.domain.announcement.domain.model.AnnouncementDetail
import com.turnin.util.db.TestDatabaseFactory
import java.time.Instant
import java.time.OffsetDateTime
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.selectAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AnnouncementRepositoryImplTest {
    private val repository = AnnouncementRepositoryImpl()

    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @After
    fun teardown() {
        TestDatabaseFactory.cleanUp()
    }

    // =============== 공지 목록 조회 ===============

    @Test
    fun `공지 목록 조회 성공 테스트 - 활성 공지만 반환한다`() = runTest {
        // given: 사용자 생성, 활성/비활성 공지 각각 생성
        val userId = insertUserAndReturnId("1")
        val activeId = createAnnouncementAndReturnId(AnnouncementStatus.ACTIVE)
        createAnnouncementAndReturnId(AnnouncementStatus.INACTIVE)

        // when
        val result = repository.getAnnouncements(userId, listOf(AnnouncementAudience.ALL))

        // then: 활성 공지만 반환
        assertEquals(1, result.size)
        assertEquals(activeId, result.first().id)
    }

    @Test
    fun `공지 목록 조회 성공 테스트 - 만료된 공지는 반환하지 않는다`() = runTest {
        // given: 만료된 공지 생성
        val userId = insertUserAndReturnId("1")
        createAnnouncementAndReturnId(
            status = AnnouncementStatus.ACTIVE,
            expiresAt = TurninDateTime.now().minusSeconds(1).toOffsetDateTime(),
        )

        // when
        val result = repository.getAnnouncements(userId, listOf(AnnouncementAudience.ALL))

        // then: 만료된 공지는 반환되지 않음
        assertEquals(0, result.size)
    }

    @Test
    fun `공지 목록 조회 성공 테스트 - 수신 대상 필터링`() = runTest {
        // given: ALL, ADMIN 대상 공지 각각 생성
        val userId = insertUserAndReturnId("1")
        val allTargetId = createAnnouncementAndReturnId(
            status = AnnouncementStatus.ACTIVE,
            targetAudience = AnnouncementAudience.ALL,
        )
        createAnnouncementAndReturnId(
            status = AnnouncementStatus.ACTIVE,
            targetAudience = AnnouncementAudience.ADMIN,
        )

        // when: 일반 사용자 수신 대상만 조회
        val result = repository.getAnnouncements(userId, listOf(AnnouncementAudience.ALL))

        // then: ALL 대상 공지만 반환
        assertEquals(1, result.size)
        assertEquals(allTargetId, result.first().id)
    }

    @Test
    fun `공지 목록 조회 성공 테스트 - 읽음 여부가 반영된다`() = runTest {
        // given: 공지 2개 생성 후 하나만 읽음 처리
        val userId = insertUserAndReturnId("1")
        val readId = createAnnouncementAndReturnId(AnnouncementStatus.ACTIVE)
        val unreadId = createAnnouncementAndReturnId(AnnouncementStatus.ACTIVE)
        repository.markAsRead(readId, userId)

        // when
        val result = repository.getAnnouncements(userId, listOf(AnnouncementAudience.ALL))

        // then
        val readAnnouncement = result.first { it.id == readId }
        val unreadAnnouncement = result.first { it.id == unreadId }
        assertTrue(readAnnouncement.isRead)
        assertFalse(unreadAnnouncement.isRead)
    }

    @Test
    fun `공지 목록 조회 성공 테스트 - 생성일 내림차순 정렬`() = runTest {
        // given: 공지 3개 생성
        val userId = insertUserAndReturnId("1")
        val id1 = createAnnouncementAndReturnId(AnnouncementStatus.ACTIVE)
        val id2 = createAnnouncementAndReturnId(AnnouncementStatus.ACTIVE)
        val id3 = createAnnouncementAndReturnId(AnnouncementStatus.ACTIVE)

        // when
        val result = repository.getAnnouncements(userId, listOf(AnnouncementAudience.ALL))

        // then: 최신순 정렬
        assertEquals(listOf(id3, id2, id1), result.map { it.id })
    }

    // =============== 공지 생성 ===============

    @Test
    fun `공지 생성 성공 테스트`() = runTest {
        // given
        val detail = AnnouncementDetail(
            title = "테스트 공지",
            content = "테스트 내용",
            targetAudience = AnnouncementAudience.ALL,
            expiresAt = null,
        )

        // when
        repository.createAnnouncement(detail)

        // then: DB에 저장됐는지 확인
        val result = TestDatabaseFactory.dbQuery {
            AnnouncementEntity.all().toList()
        }
        assertEquals(1, result.size)
        assertEquals(detail.title, result.first().title)
        assertEquals(AnnouncementStatus.INACTIVE, result.first().status) // 기본값 INACTIVE
    }

    @Test
    fun `공지 생성 성공 테스트 - 만료일 설정`() = runTest {
        // given
        val expiresAt = TurninDateTime.now().plusSeconds(1.days.inWholeSeconds).epochSecond
        val detail = AnnouncementDetail(
            title = "만료일 있는 공지",
            content = "내용",
            targetAudience = AnnouncementAudience.ALL,
            expiresAt = expiresAt,
        )

        // when
        repository.createAnnouncement(detail)

        // then
        val result = TestDatabaseFactory.dbQuery {
            AnnouncementEntity.all().first()
        }
        assertNotNull(result.expiresAt)
    }

    // =============== 읽음 처리 ===============

    @Test
    fun `공지 읽음 처리 성공 테스트`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        val announcementId = createAnnouncementAndReturnId(AnnouncementStatus.ACTIVE)

        // when
        repository.markAsRead(announcementId, userId)

        // then
        val result = TestDatabaseFactory.dbQuery {
            AnnouncementReads
                .selectAll()
                .where { AnnouncementReads.announcementId eq announcementId.value }
                .toList()
        }
        assertEquals(1, result.size)
    }

    @Test
    fun `공지 읽음 처리 성공 테스트 - 중복 읽음은 무시한다`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        val announcementId = createAnnouncementAndReturnId(AnnouncementStatus.ACTIVE)

        // when: 2번 읽음 처리
        repository.markAsRead(announcementId, userId)
        repository.markAsRead(announcementId, userId)

        // then: 1개만 저장
        val result = TestDatabaseFactory.dbQuery {
            AnnouncementReads
                .selectAll()
                .where { AnnouncementReads.announcementId eq announcementId.value }
                .toList()
        }
        assertEquals(1, result.size)
    }

    // =============== 공지 상태 변경 ===============

    @Test
    fun `공지 상태 변경 성공 테스트`() = runTest {
        // given
        val announcementId = createAnnouncementAndReturnId(AnnouncementStatus.INACTIVE)

        // when
        val result = repository.updateStatus(announcementId, AnnouncementStatus.ACTIVE)

        // then
        assertTrue(result)
        val updated = TestDatabaseFactory.dbQuery { AnnouncementEntity.findById(announcementId.value) }
        assertEquals(AnnouncementStatus.ACTIVE, updated?.status)
    }

    @Test
    fun `공지 상태 변경 실패 테스트 - 존재하지 않는 공지`() = runTest {
        // when
        val result = repository.updateStatus(AnnouncementId(999L), AnnouncementStatus.ACTIVE)

        // then
        assertFalse(result)
    }

    // =============== 공지 삭제 ===============

    @Test
    fun `공지 삭제 성공 테스트`() = runTest {
        // given
        val announcementId = createAnnouncementAndReturnId(AnnouncementStatus.ACTIVE)

        // when
        val result = repository.deleteAnnouncement(announcementId)

        // then
        assertTrue(result)
        val deleted = TestDatabaseFactory.dbQuery { AnnouncementEntity.findById(announcementId.value) }
        assertNull(deleted)
    }

    @Test
    fun `공지 삭제 실패 테스트 - 존재하지 않는 공지`() = runTest {
        // when
        val result = repository.deleteAnnouncement(AnnouncementId(999L))

        // then
        assertFalse(result)
    }

    // =============== 헬퍼 함수 ===============

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

    private suspend fun createAnnouncementAndReturnId(
        status: AnnouncementStatus,
        targetAudience: AnnouncementAudience = AnnouncementAudience.ALL,
        expiresAt: OffsetDateTime? = null,
    ): AnnouncementId = TestDatabaseFactory.dbQuery {
        val entity = AnnouncementEntity.new {
            this.title = "공지 제목"
            this.content = "공지 내용"
            this.targetAudience = targetAudience
            this.status = status
            this.expiresAt = expiresAt
        }
        AnnouncementId(entity.id.value)
    }
}
