package com.turnin.domain.account.application

import com.turnin.common.db.schema.AnnouncementEntity
import com.turnin.common.db.schema.AnnouncementReads
import com.turnin.common.db.schema.BlockEntity
import com.turnin.common.db.schema.BlockReasons
import com.turnin.common.db.schema.Blocks
import com.turnin.common.db.schema.ContentReports
import com.turnin.common.db.schema.FriendEntity
import com.turnin.common.db.schema.Friends
import com.turnin.common.db.schema.KeywordEntity
import com.turnin.common.db.schema.NotificationEntity
import com.turnin.common.db.schema.Notifications
import com.turnin.common.db.schema.PingPongAnswers
import com.turnin.common.db.schema.PingPongs
import com.turnin.common.db.schema.RefreshTokens
import com.turnin.common.db.schema.ReportReasons
import com.turnin.common.db.schema.Reports
import com.turnin.common.db.schema.UserEntity
import com.turnin.common.db.schema.UserKeywordEntity
import com.turnin.common.db.schema.UserKeywords
import com.turnin.common.db.schema.Users
import com.turnin.common.model.AnnouncementAudience
import com.turnin.common.model.AnnouncementStatus
import com.turnin.common.model.ContentReportType
import com.turnin.common.model.FriendRequestStatus
import com.turnin.common.model.NotificationType
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.domain.account.exception.AccountException
import com.turnin.domain.announcement.application.provider.AnnouncementDeletionSupportApi
import com.turnin.domain.announcement.infrastructure.repository.AnnouncementRepositoryImpl
import com.turnin.domain.auth.application.provider.AuthDeletionSupportApi
import com.turnin.domain.auth.infrastructure.repository.impl.RefreshTokenRepositoryImpl
import com.turnin.domain.block.application.provider.BlockDeletionSupportApi
import com.turnin.domain.block.infrastructure.repository.BlockRepositoryImpl
import com.turnin.domain.file.application.provider.FileDeletionSupportApi
import com.turnin.domain.friend.application.provider.FriendDeletionSupportApi
import com.turnin.domain.friend.infrastructure.repository.FriendRepositoryImpl
import com.turnin.domain.notification.application.provider.NotificationDeletionSupportApi
import com.turnin.domain.notification.infrastructure.repository.FcmTokenRepositoryImpl
import com.turnin.domain.notification.infrastructure.repository.NotificationRepositoryImpl
import com.turnin.domain.pingPong.application.provider.PingPongDeletionSupportApi
import com.turnin.domain.pingPong.infrastructure.repository.PingPongRepositoryImpl
import com.turnin.domain.user.application.dto.toDto
import com.turnin.domain.user.application.provider.UserDeletionSupportApi
import com.turnin.domain.user.domain.model.User
import com.turnin.domain.user.infrastructure.mapper.UserMapper.toDomain
import com.turnin.domain.user.infrastructure.repository.impl.UserRepositoryImpl
import com.turnin.domain.userKeyword.application.provider.UserKeywordDeletionSupportApi
import com.turnin.domain.userKeyword.infrastructure.repository.impl.UserKeywordRepositoryImpl
import com.turnin.util.db.TestDatabaseFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class DeleteAccountUseCaseIntegrationTest {
    private val mockFileDeletionSupportApi = mockk<FileDeletionSupportApi>(relaxed = true)

    private val usecase = DeleteAccountUseCase(
        authDeletionSupportApi = AuthDeletionSupportApi(RefreshTokenRepositoryImpl()),
        userDeletionSupportApi = UserDeletionSupportApi(UserRepositoryImpl()),
        friendDeletionSupportApi = FriendDeletionSupportApi(FriendRepositoryImpl()),
        blockDeletionSupportApi = BlockDeletionSupportApi(BlockRepositoryImpl()),
        userKeywordDeletionSupportApi = UserKeywordDeletionSupportApi(UserKeywordRepositoryImpl()),
        fileDeletionSupportApi = mockFileDeletionSupportApi,
        notificationDeletionSupportApi = NotificationDeletionSupportApi(
            NotificationRepositoryImpl(),
            FcmTokenRepositoryImpl(),
        ),
        pingPongDeletionSupportApi = PingPongDeletionSupportApi(PingPongRepositoryImpl()),
        announcementDeletionSupportApi = AnnouncementDeletionSupportApi(AnnouncementRepositoryImpl()),
    )

    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @After
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `계정 삭제 성공 - 프로필 이미지가 있는 경우`() = runTest {
        // given
        val user = insertUser("1", profileImageUrl = "https://r2.example.com/profile.jpg")
        val other = insertUser("2", profileImageUrl = "https://r2.example.com/profile2.jpg")
        val originalProviderId = user.providerId
        insertRefreshToken(user.id.value)
        insertFriend(user.id.value, other.id.value)
        insertBlock(user.id.value, other.id.value)
        insertUserKeyword(user.id.value)
        insertNotification(user.id.value)

        // when
        usecase(user.id.value)

        // then
        // 사용자 비활성화 검증
        val foundUser = findUserByIdForTest(user.id.value)
        assertNotNull(foundUser)
        assertFalse(foundUser!!.isActive)

        // providerId 비식별화 검증
        assertTrue(foundUser.providerId.startsWith("DELETED_"))
        assertTrue(foundUser.providerId.endsWith(originalProviderId))
        assertNotEquals(originalProviderId, foundUser.providerId)

        // 신고 내역이 없는 user_keyword 삭제 검증
        val userKeywords = findUserKeywordsByUserIdForTest(user.id.value)
        assertTrue(userKeywords.isEmpty())

        // friend 하드 삭제 검증
        val friends = findFriendsByUserIdForTest(user.id.value)
        assertTrue(friends.isEmpty())

        // block 하드 삭제 검증
        val blocks = findBlocksByUserIdForTest(user.id.value)
        assertTrue(blocks.isEmpty())

        // refresh token 하드 삭제 검증
        val refreshToken = findRefreshTokenByUserIdForTest(user.id.value)
        assertNull(refreshToken)

        // 파일 삭제 검증
        coVerify(exactly = 1) {
            mockFileDeletionSupportApi.deleteFile("https://r2.example.com/profile.jpg")
        }

        // notification 삭제 검증
        val notifications = findNotificationsByUserIdForTest(user.id.value)
        assertTrue(notifications.isEmpty())

        // other 사용자는 영향받지 않아야 함
        val otherUser = findUserByIdForTest(other.id.value)
        assertNotNull(otherUser)
        assertTrue(otherUser!!.isActive)
    }

    @Test
    fun `계정 삭제 성공 - 프로필 이미지가 없는 경우`() = runTest {
        // given
        val user = insertUser("1", profileImageUrl = null)
        val originalProviderId = user.providerId

        // when
        usecase(user.id.value)

        // then
        val foundUser = findUserByIdForTest(user.id.value)
        assertNotNull(foundUser)
        assertFalse(foundUser!!.isActive)

        // providerId 비식별화 검증
        assertTrue(foundUser.providerId.startsWith("DELETED_"))
        assertTrue(foundUser.providerId.endsWith(originalProviderId))
        assertNotEquals(originalProviderId, foundUser.providerId)

        // 파일 삭제 호출 안됨 검증
        coVerify(exactly = 0) { mockFileDeletionSupportApi.deleteFile(any()) }
    }

    @Test
    fun `계정 삭제 실패 - 존재하지 않는 사용자`() = runTest {
        // given
        val notExistUserId = 999L

        // when & then
        assertThrows<AccountException.UserNotFound> {
            usecase(notExistUserId)
        }
    }

    @Test
    fun `계정 삭제 성공 - 파일 삭제 실패 시 DB는 롤백되지 않는다`() = runTest {
        // given
        val user = insertUser("1", profileImageUrl = "https://r2.example.com/profile.jpg")
        val originalProviderId = user.providerId
        coEvery {
            mockFileDeletionSupportApi.deleteFile(any())
        } throws RuntimeException("R2 connection failed")

        // when
        usecase(user.id.value) // 파일 삭제 실패해도 예외가 전파되지 않아야 함

        // then
        // DB 작업은 정상적으로 완료됐는지 검증
        val foundUser = findUserByIdForTest(user.id.value)
        assertNotNull(foundUser)
        assertFalse(foundUser!!.isActive)
        assertTrue(foundUser.providerId.startsWith("DELETED_"))
        assertTrue(foundUser.providerId.endsWith(originalProviderId))

        // 파일 삭제 시도는 했는지 검증
        coVerify(exactly = 1) {
            mockFileDeletionSupportApi.deleteFile("https://r2.example.com/profile.jpg")
        }
    }

    @Test
    fun `계정 삭제 실패 - DB 작업 실패 시 롤백된다`() = runTest {
        // given
        val user = insertUser("1", profileImageUrl = "https://r2.example.com/profile.jpg")
        val other = insertUser("2", profileImageUrl = null)
        insertRefreshToken(user.id.value)
        insertFriend(user.id.value, other.id.value)
        insertBlock(user.id.value, other.id.value)
        insertUserKeyword(user.id.value)
        insertNotification(user.id.value)

        // userDeletionSupportApi.deactivate() 호출 시 예외 발생 (트랜잭션 중간 실패 시뮬레이션)
        val mockUserDeletionSupportApi = mockk<UserDeletionSupportApi> {
            coEvery { findById(user.id) } returns user.toDto()
            coEvery { anonymizeProviderId(user.id, any()) } returns true
            coEvery { deactivate(user.id) } throws RuntimeException("DB connection failed")
        }

        val failingUseCase = DeleteAccountUseCase(
            authDeletionSupportApi = AuthDeletionSupportApi(RefreshTokenRepositoryImpl()),
            userDeletionSupportApi = mockUserDeletionSupportApi,
            friendDeletionSupportApi = FriendDeletionSupportApi(FriendRepositoryImpl()),
            blockDeletionSupportApi = BlockDeletionSupportApi(BlockRepositoryImpl()),
            userKeywordDeletionSupportApi = UserKeywordDeletionSupportApi(UserKeywordRepositoryImpl()),
            fileDeletionSupportApi = mockFileDeletionSupportApi,
            notificationDeletionSupportApi = NotificationDeletionSupportApi(
                NotificationRepositoryImpl(),
                FcmTokenRepositoryImpl(),
            ),
            pingPongDeletionSupportApi = PingPongDeletionSupportApi(PingPongRepositoryImpl()),
            announcementDeletionSupportApi = AnnouncementDeletionSupportApi(AnnouncementRepositoryImpl()),
        )

        // when & then
        assertThrows<RuntimeException> {
            failingUseCase(user.id.value)
        }

        // then: 롤백 검증 - 모든 데이터가 원래 상태로 유지되어야 함
        // 사용자 활성화 상태 유지 검증
        val foundUser = findUserByIdForTest(user.id.value)
        assertNotNull(foundUser)
        assertTrue(foundUser!!.isActive)
        assertEquals(user.providerId, foundUser.providerId) // providerId 변조 안됨

        // friend 롤백 검증
        val friends = findFriendsByUserIdForTest(user.id.value)
        assertFalse(friends.isEmpty())

        // block 롤백 검증
        val blocks = findBlocksByUserIdForTest(user.id.value)
        assertFalse(blocks.isEmpty())

        // refresh token 롤백 검증
        val refreshToken = findRefreshTokenByUserIdForTest(user.id.value)
        assertNotNull(refreshToken)

        // user_keyword 롤백 검증
        val userKeywords = findUserKeywordsByUserIdForTest(user.id.value)
        assertTrue(userKeywords.all { it.isActive })

        // notification 롤백 검증
        val notifications = findNotificationsByUserIdForTest(user.id.value)
        assertFalse(notifications.isEmpty())

        // 파일 삭제 호출 안됨 검증 (트랜잭션 실패로 파일 삭제 단계까지 도달하지 않아야 함)
        coVerify(exactly = 0) {
            mockFileDeletionSupportApi.deleteFile(any())
        }
    }

    @Test
    fun `계정 삭제 시 신고 내역이 있는 게시물은 비활성화되고 삭제 시각이 기록되어 남는다`() = runTest {
        // given
        val user = insertUser("1", profileImageUrl = null)
        val reporter = insertUser("2", profileImageUrl = null)
        val userKeyword = insertUserKeyword(user.id.value)
        insertUserKeywordReport(reporterId = reporter.id.value, userKeywordId = userKeyword.id.value)

        // when
        usecase(user.id.value)

        // then
        val foundUserKeyword = findUserKeywordByIdForTest(userKeyword.id.value)
        assertNotNull(foundUserKeyword)
        assertFalse(foundUserKeyword!!.isActive)
        assertNotNull(foundUserKeyword.deletedAt)
    }

    @Test
    fun `계정 삭제 시 사용자가 다른 게시물에 남긴 질문과 답변이 삭제된다`() = runTest {
        // given
        val user = insertUser("1", profileImageUrl = null)
        val owner = insertUser("2", profileImageUrl = null)
        val ownerUserKeyword = insertUserKeyword(owner.id.value)
        val pingPongId = insertPingPong(userKeywordId = ownerUserKeyword.id.value, questionerId = user.id.value)
        val answerId = insertPingPongAnswer(pingPongId)

        // when
        usecase(user.id.value)

        // then
        assertEquals(0L, countPingPongByIdForTest(pingPongId))
        assertEquals(0L, countPingPongAnswerByIdForTest(answerId))
    }

    @Test
    fun `계정 삭제 시 사용자의 신고된 게시물에 달린 핑퐁이 삭제된다`() = runTest {
        // given: 신고된 게시물은 비활성화로 남으므로 CASCADE 없이 핑퐁이 삭제되어야 한다.
        val user = insertUser("1", profileImageUrl = null)
        val questioner = insertUser("2", profileImageUrl = null)
        val userKeyword = insertUserKeyword(user.id.value)
        insertUserKeywordReport(reporterId = questioner.id.value, userKeywordId = userKeyword.id.value)
        val pingPongId = insertPingPong(userKeywordId = userKeyword.id.value, questionerId = questioner.id.value)
        val answerId = insertPingPongAnswer(pingPongId)

        // when
        usecase(user.id.value)

        // then
        assertEquals(0L, countPingPongByIdForTest(pingPongId))
        assertEquals(0L, countPingPongAnswerByIdForTest(answerId))
    }

    @Test
    fun `계정 삭제 시 신고된 핑퐁의 신고 스냅샷은 유지된다`() = runTest {
        // given
        val user = insertUser("1", profileImageUrl = null)
        val owner = insertUser("2", profileImageUrl = null)
        val ownerUserKeyword = insertUserKeyword(owner.id.value)
        val pingPongId = insertPingPong(userKeywordId = ownerUserKeyword.id.value, questionerId = user.id.value)
        val contentReportId = insertPingPongQuestionReport(
            reporterId = owner.id.value,
            reportedUserId = user.id.value,
            pingPongId = pingPongId,
            snapshot = "신고된 질문",
        )

        // when
        usecase(user.id.value)

        // then
        assertEquals(0L, countPingPongByIdForTest(pingPongId))
        assertEquals("신고된 질문", findContentReportSnapshotForTest(contentReportId))
    }

    @Test
    fun `계정 삭제 시 사용자의 공지 읽음 기록이 삭제된다`() = runTest {
        // given
        val user = insertUser("1", profileImageUrl = null)
        insertAnnouncementRead(user.id.value)

        // when
        usecase(user.id.value)

        // then
        val count = TestDatabaseFactory.dbQuery {
            AnnouncementReads.selectAll().where { AnnouncementReads.userId eq user.id.value }.count()
        }
        assertEquals(0L, count)
    }

    // ------------------------------ Test Utils ------------------------------

    private suspend fun insertUser(
        uniqueValue: String,
        profileImageUrl: String?,
    ): User = TestDatabaseFactory.dbQuery {
        UserEntity
            .new {
                this.role = Role.USER
                this.provider = SocialLoginProvider.GOOGLE
                this.providerId = "pid$uniqueValue"
                this.displayId = "did$uniqueValue"
                this.name = "honggd$uniqueValue"
                this.profileImageUrl = profileImageUrl
                this.introduce = "hello$uniqueValue"
                this.isActive = true
                this.lastLoginAt = Instant.now()
            }.toDomain()
    }

    private suspend fun insertRefreshToken(userId: Long) = TestDatabaseFactory.dbQuery {
        RefreshTokens
            .upsert {
                it[user] = EntityID(userId, Users)
                it[refreshToken] = "test.refresh.token"
            }
    }

    private suspend fun insertFriend(userId1: Long, userId2: Long) = TestDatabaseFactory.dbQuery {
        FriendEntity.new {
            this.requesterId = EntityID(userId1, Users)
            this.receiverId = EntityID(userId2, Users)
            this.status = FriendRequestStatus.ACCEPTED
            this.respondedAt = null
        }
    }

    private suspend fun insertBlock(userId1: Long, userId2: Long) = TestDatabaseFactory.dbQuery {
        BlockEntity.new {
            this.blockerId = EntityID(userId1, Users)
            this.blockedId = EntityID(userId2, Users)
            this.reasonId = EntityID(1, BlockReasons)
        }
    }

    private suspend fun insertUserKeyword(userId: Long) = TestDatabaseFactory.dbQuery {
        val keyword = KeywordEntity.new {
            this.keyword = "keyword"
            this.embedding = "[1, 1, 1]"
            this.createdBy = EntityID(userId, Users)
        }

        UserKeywordEntity.new {
            this.userId = EntityID(userId, Users)
            this.keywordId = keyword.id
            this.description = "description"
            this.isActive = true
        }
    }

    private suspend fun findUserByIdForTest(userId: Long): UserEntity? = TestDatabaseFactory.dbQuery {
        UserEntity.findById(userId)
    }

    private suspend fun findUserKeywordsByUserIdForTest(userId: Long): List<UserKeywordEntity> =
        TestDatabaseFactory.dbQuery {
            UserKeywordEntity
                .find {
                    UserKeywords.userId eq userId
                }.toList()
        }

    private suspend fun findFriendsByUserIdForTest(userId: Long): List<FriendEntity> =
        TestDatabaseFactory.dbQuery {
            FriendEntity
                .find {
                    (Friends.requesterId eq userId) or
                        (Friends.receiverId eq userId)
                }.toList()
        }

    private suspend fun findBlocksByUserIdForTest(userId: Long): List<BlockEntity> =
        TestDatabaseFactory.dbQuery {
            BlockEntity
                .find {
                    (Blocks.blockerId eq userId) or
                        (Blocks.blockedId eq userId)
                }.toList()
        }

    private suspend fun findRefreshTokenByUserIdForTest(userId: Long): String? =
        TestDatabaseFactory.dbQuery {
            RefreshTokens
                .selectAll()
                .where { RefreshTokens.user eq userId }
                .map { it[RefreshTokens.refreshToken] }
                .singleOrNull()
        }

    private suspend fun insertAnnouncementRead(userId: Long) = TestDatabaseFactory.dbQuery {
        val announcement = AnnouncementEntity.new {
            this.title = "공지 제목"
            this.content = "공지 내용"
            this.targetAudience = AnnouncementAudience.ALL
            this.status = AnnouncementStatus.ACTIVE
        }
        AnnouncementReads.insert {
            it[announcementId] = announcement.id.value
            it[AnnouncementReads.userId] = userId
        }
    }

    private suspend fun findUserKeywordByIdForTest(userKeywordId: Long): UserKeywordEntity? =
        TestDatabaseFactory.dbQuery {
            UserKeywordEntity.findById(userKeywordId)
        }

    private suspend fun insertUserKeywordReport(reporterId: Long, userKeywordId: Long) = TestDatabaseFactory.dbQuery {
        Reports.insert {
            it[Reports.reporterId] = EntityID(reporterId, Users)
            it[reportedUserKeywordId] = EntityID(userKeywordId, UserKeywords)
            it[reasonId] = EntityID(1L, ReportReasons)
        }
    }

    private suspend fun insertPingPong(userKeywordId: Long, questionerId: Long): Long = TestDatabaseFactory.dbQuery {
        PingPongs.insertAndGetId {
            it[PingPongs.userKeywordId] = EntityID(userKeywordId, UserKeywords)
            it[PingPongs.questionerId] = EntityID(questionerId, Users)
            it[question] = "질문 내용"
        }.value
    }

    private suspend fun insertPingPongAnswer(pingPongId: Long): Long = TestDatabaseFactory.dbQuery {
        PingPongAnswers.insertAndGetId {
            it[PingPongAnswers.pingPongId] = EntityID(pingPongId, PingPongs)
            it[answer] = "답변 내용"
        }.value
    }

    private suspend fun insertPingPongQuestionReport(
        reporterId: Long,
        reportedUserId: Long,
        pingPongId: Long,
        snapshot: String,
    ): Long = TestDatabaseFactory.dbQuery {
        ContentReports.insertAndGetId {
            it[ContentReports.reporterId] = EntityID(reporterId, Users)
            it[ContentReports.reportedUserId] = EntityID(reportedUserId, Users)
            it[contentType] = ContentReportType.PING_PONG_QUESTION
            it[contentId] = pingPongId
            it[contentSnapshot] = snapshot
            it[reasonId] = EntityID(1L, ReportReasons)
        }.value
    }

    private suspend fun countPingPongByIdForTest(pingPongId: Long): Long = TestDatabaseFactory.dbQuery {
        PingPongs.selectAll().where { PingPongs.id eq pingPongId }.count()
    }

    private suspend fun countPingPongAnswerByIdForTest(answerId: Long): Long = TestDatabaseFactory.dbQuery {
        PingPongAnswers.selectAll().where { PingPongAnswers.id eq answerId }.count()
    }

    private suspend fun findContentReportSnapshotForTest(contentReportId: Long): String? =
        TestDatabaseFactory.dbQuery {
            ContentReports
                .selectAll()
                .where { ContentReports.id eq contentReportId }
                .singleOrNull()
                ?.get(ContentReports.contentSnapshot)
        }

    private suspend fun insertNotification(userId: Long) = TestDatabaseFactory.dbQuery {
        NotificationEntity.new {
            this.userId = EntityID(userId, Users)
            this.notiType = NotificationType.NEW_KEYWORD
            this.message = "test-message"
        }
    }

    private suspend fun findNotificationsByUserIdForTest(userId: Long): List<NotificationEntity> =
        TestDatabaseFactory.dbQuery {
            NotificationEntity
                .find {
                    Notifications.userId eq userId
                }.toList()
        }
}
