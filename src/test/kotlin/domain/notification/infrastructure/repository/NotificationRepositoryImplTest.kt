package com.peekr.domain.notification.infrastructure.repository

import com.peekr.common.db.schema.UserEntity
import com.peekr.common.model.NotificationType
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.id.NotificationId
import com.peekr.common.model.id.UserId
import com.peekr.domain.notification.domain.model.NotificationCommand
import com.peekr.util.db.TestDatabaseFactory
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotificationRepositoryImplTest {
    private val repository = NotificationRepositoryImpl()

    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @After
    fun teardown() {
        TestDatabaseFactory.cleanUp()
    }

    // ======================== save ========================

    @Test
    fun `개인 알림 저장 성공`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        val command = NotificationCommand.personal(
            userId = userId,
            notiType = NotificationType.FRIEND_REQUEST,
            title = "친구 요청",
            message = "테스트 유저님이 친구 요청을 보냈어요.",
            imageUrl = null,
            refId = 1L,
            refType = "USER",
        )

        // when
        val result = repository.save(command)

        // then
        assertNotNull(result)
        assertEquals(userId.value, result.userId?.value)
        assertEquals(NotificationType.FRIEND_REQUEST, result.notiType)
        assertEquals("친구 요청", result.title)
        assertFalse(result.isRead)
        assertFalse(result.isBroadcast)
    }

    @Test
    fun `브로드캐스트 알림 저장 성공`() = runTest {
        // given
        val command = NotificationCommand.broadcast(
            notiType = NotificationType.NOTICE,
            title = "공지사항",
            message = "서비스 점검 안내입니다.",
        )

        // when
        val result = repository.save(command)

        // then
        assertNull(result.userId)
        assertTrue(result.isBroadcast)
        assertEquals(NotificationType.NOTICE, result.notiType)
    }

    @Test
    fun `title 이 null 인 알림 저장 성공`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        val command = NotificationCommand.personal(
            userId = userId,
            notiType = NotificationType.FRIEND_ACCEPT,
            title = null,
            message = "친구 요청이 수락됐어요.",
        )

        // when
        val result = repository.save(command)

        // then
        assertNull(result.title)
    }

    // ======================== findByUserId ========================

    @Test
    fun `알림 목록 최신순 조회 성공`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        repeat(3) {
            repository.save(
                NotificationCommand.personal(
                    userId = userId,
                    notiType = NotificationType.FRIEND_REQUEST,
                    title = "친구 요청 $it",
                    message = "message $it",
                ),
            )
        }

        // when
        val result = repository.findByUserId(userId, cursor = null, size = 10)

        // then
        assertEquals(3, result.size)
        // 최신순 정렬 검증
        assertTrue(result.first().id.value > result.last().id.value)
    }

    @Test
    fun `브로드캐스트 알림도 함께 조회된다`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        repository.save(
            NotificationCommand.personal(
                userId = userId,
                notiType = NotificationType.FRIEND_REQUEST,
                title = "친구 요청",
                message = "message",
            ),
        )
        repository.save(
            NotificationCommand.broadcast(
                notiType = NotificationType.NOTICE,
                title = "공지",
                message = "공지 내용",
            ),
        )

        // when
        val result = repository.findByUserId(userId, cursor = null, size = 10)

        // then
        assertEquals(2, result.size)
    }

    @Test
    fun `커서 기반 페이지네이션이 정상 동작한다`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        repeat(5) {
            repository.save(
                NotificationCommand.personal(
                    userId = userId,
                    notiType = NotificationType.FRIEND_REQUEST,
                    title = "친구 요청 $it",
                    message = "message $it",
                ),
            )
        }

        // when: 첫 페이지 (size = 3 → 실제 조회는 size + 1 = 4개)
        val firstPage = repository.findByUserId(userId, cursor = null, size = 3)

        // 실제 페이지 데이터는 3개, 4번째는 다음 페이지 존재 확인용 extra
        // 커서는 실제 페이지 마지막 데이터(3번째)의 id
        val nextCursor = firstPage[2].id.value

        // when: 두 번째 페이지 (cursor = 실제 페이지 마지막 id)
        val secondPage = repository.findByUserId(
            userId,
            cursor = nextCursor,
            size = 3,
        )

        // then
        // 첫 페이지: size + 1 = 4개 (extra 1개 포함)
        assertEquals(4, firstPage.size)
        // 두 번째 페이지: 남은 데이터 2개 (extra 없음 → 다음 페이지 없음)
        assertEquals(2, secondPage.size)
        // 중복 없음 검증
        val firstPageIds = firstPage.take(3).map { it.id.value }.toSet() // extra 제외
        val secondPageIds = secondPage.map { it.id.value }.toSet()
        assertTrue(firstPageIds.intersect(secondPageIds).isEmpty())
    }

    @Test
    fun `알림이 없으면 빈 리스트를 반환한다`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")

        // when
        val result = repository.findByUserId(userId, cursor = null, size = 10)

        // then
        assertTrue(result.isEmpty())
    }

    // ======================== markAsRead ========================

    @Test
    fun `알림 읽음 처리 성공`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        val notification = repository.save(
            NotificationCommand.personal(
                userId = userId,
                notiType = NotificationType.FRIEND_REQUEST,
                title = "친구 요청",
                message = "message",
            ),
        )
        assertFalse(notification.isRead)

        // when
        val result = repository.markAsRead(notification.id, userId)

        // then
        assertTrue(result)
        val notifications = repository.findByUserId(userId, cursor = null, size = 10)
        assertTrue(notifications.first().isRead)
    }

    @Test
    fun `존재하지 않는 알림 읽음 처리 시 false 반환`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")

        // when
        val result = repository.markAsRead(NotificationId(999L), userId)

        // then
        assertFalse(result)
    }

    @Test
    fun `다른 사용자의 알림은 읽음 처리할 수 없다`() = runTest {
        // given
        val userId1 = insertUserAndReturnId("1")
        val userId2 = insertUserAndReturnId("2")
        val notification = repository.save(
            NotificationCommand.personal(
                userId = userId1,
                notiType = NotificationType.FRIEND_REQUEST,
                title = "친구 요청",
                message = "message",
            ),
        )

        // when: userId2 로 userId1 의 알림 읽음 처리 시도
        val result = repository.markAsRead(notification.id, userId2)

        // then
        assertFalse(result)
        val notifications = repository.findByUserId(userId1, cursor = null, size = 10)
        assertFalse(notifications.first().isRead) // userId1 의 알림은 그대로
    }

    // ======================== helper ========================

    private suspend fun insertUserAndReturnId(uniqueValue: String): UserId =
        TestDatabaseFactory.dbQuery {
            val savedUser = UserEntity.new {
                this.role = Role.USER
                this.provider = SocialLoginProvider.GOOGLE
                this.providerId = "pid$uniqueValue"
                this.displayId = "did$uniqueValue"
                this.name = "testUser$uniqueValue"
                this.profileImageUrl = null
                this.introduce = "hello"
                this.isActive = true
                this.lastLoginAt = Instant.now()
            }
            UserId(savedUser.id.value)
        }
}
