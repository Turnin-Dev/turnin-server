package com.turnin.common.batch

import com.turnin.common.db.schema.AnnouncementEntity
import com.turnin.common.db.schema.AnnouncementReads
import com.turnin.common.db.schema.BlockReasons
import com.turnin.common.db.schema.Blocks
import com.turnin.common.db.schema.Friends
import com.turnin.common.db.schema.KeywordEntity
import com.turnin.common.db.schema.Keywords
import com.turnin.common.db.schema.Notifications
import com.turnin.common.db.schema.RefreshTokens
import com.turnin.common.db.schema.ReportReasons
import com.turnin.common.db.schema.Reports
import com.turnin.common.db.schema.UserEntity
import com.turnin.common.db.schema.UserFcmTokens
import com.turnin.common.db.schema.UserKeywordEntity
import com.turnin.common.db.schema.UserKeywords
import com.turnin.common.db.schema.Users
import com.turnin.common.infrastructure.di.infraModule
import com.turnin.common.model.AnnouncementAudience
import com.turnin.common.model.AnnouncementStatus
import com.turnin.common.model.FriendRequestStatus
import com.turnin.common.model.NotificationType
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.util.TurninDateTime
import com.turnin.common.util.toOffsetDateTime
import com.turnin.domain.account.application.DeleteAccountUseCase
import com.turnin.domain.account.di.accountModule
import com.turnin.domain.announcement.di.announcementModule
import com.turnin.domain.auth.di.authModule
import com.turnin.domain.block.di.blockModule
import com.turnin.domain.file.application.provider.FileDeletionSupportApi
import com.turnin.domain.file.di.fileModule
import com.turnin.domain.friend.di.friendModule
import com.turnin.domain.keyword.di.keywordModule
import com.turnin.domain.notification.di.notificationModule
import com.turnin.domain.report.di.reportModule
import com.turnin.domain.user.di.userModule
import com.turnin.domain.userKeyword.di.userKeywordModule
import com.turnin.util.db.TestDatabaseFactory
import io.mockk.mockk
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject

class AccountDeletionIntegrationTest : KoinTest {
    private val fileDeletionSupportApi: FileDeletionSupportApi = mockk(relaxed = true)

    @Before
    fun setUp() {
        TestDatabaseFactory.init()
        startKoin {
            modules(
                authModule,
                userModule,
                fileModule,
                keywordModule,
                userKeywordModule,
                reportModule,
                friendModule,
                blockModule,
                accountModule,
                notificationModule,
                announcementModule,
                infraModule,
                batchModule,
                module {
                    // R2 등 외부 의존성 Mock 오버라이드
                    single<FileDeletionSupportApi> { fileDeletionSupportApi }
                },
            )
        }
    }

    @After
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
        stopKoin()
    }

    private val deleteAccountUseCase: DeleteAccountUseCase by inject()
    private val hardDeleteExpiredAccountsBatch: HardDeleteExpiredAccountsBatch by inject()

    @Test
    fun `계정 탈퇴(SoftDelete) 후 1년 경과 시 배치(HardDelete)가 모든 데이터를 삭제한다`() = runTest {
        // given: 사용자 및 모든 연관 데이터 생성
        val targetUser = insertUser("target")
        val otherUser = insertUser("other")
        val targetId = targetUser.id.value
        val keyword = insertKeyword("keyword")

        insertRefreshToken(targetId)
        insertNotification(targetId)
        insertUserFcmToken(targetId)
        insertAnnouncementRead(targetId)
        insertFriend(requesterId = targetId, receiverId = otherUser.id.value)
        insertFriend(requesterId = otherUser.id.value, receiverId = targetId)
        insertBlock(blockerId = targetId, blockedId = otherUser.id.value)
        insertUserKeyword(userId = targetId, keywordId = keyword.id.value)
        insertReport(reporterId = otherUser.id.value, reportedId = targetId)

        // when 1: 탈퇴 (SoftDelete)
        deleteAccountUseCase(targetId)

        // then 1: 사용자 row는 남아있고 비활성화 상태
        TestDatabaseFactory.dbQuery {
            val user = UserEntity.findById(targetId)
            assertNotNull(user)
            assertFalse(user!!.isActive)
            assertNotNull(user.deletedAt)
        }

        // when 2: 1년 경과 후 배치 실행 - deletedAt을 1년 전으로 조작
        TestDatabaseFactory.dbQuery {
            Users.update({ Users.id eq targetId }) {
                it[deletedAt] = TurninDateTime.now().minus(366.days.toJavaDuration())
            }
        }
        hardDeleteExpiredAccountsBatch.run()

        // then 2: 모든 데이터 완전 삭제 검증
        TestDatabaseFactory.dbQuery {
            // 사용자 row 완전 삭제
            assertNull(UserEntity.findById(targetId))

            // 명시적 삭제 (RESTRICT)
            assertTrue(
                Reports
                    .selectAll()
                    .where { (Reports.reporterId eq targetId) or (Reports.reportedId eq targetId) }
                    .empty(),
            )
            assertTrue(
                UserKeywords
                    .selectAll()
                    .where { UserKeywords.userId eq targetId }
                    .empty(),
            )
            assertTrue(
                Friends
                    .selectAll()
                    .where { (Friends.requesterId eq targetId) or (Friends.receiverId eq targetId) }
                    .empty(),
            )
            assertTrue(
                Blocks
                    .selectAll()
                    .where { (Blocks.blockerId eq targetId) or (Blocks.blockedId eq targetId) }
                    .empty(),
            )

            // 자동 삭제 (CASCADE)
            assertTrue(
                RefreshTokens
                    .selectAll()
                    .where { RefreshTokens.user eq targetId }
                    .empty(),
            )
            assertTrue(
                Notifications
                    .selectAll()
                    .where { Notifications.userId eq targetId }
                    .empty(),
            )
            assertTrue(
                UserFcmTokens
                    .selectAll()
                    .where { UserFcmTokens.userId eq targetId }
                    .empty(),
            )
            assertTrue(
                AnnouncementReads
                    .selectAll()
                    .where { AnnouncementReads.userId eq targetId }
                    .empty(),
            )

            // 다른 사용자 데이터는 그대로
            assertNotNull(UserEntity.findById(otherUser.id.value))
        }
    }

    // --- 헬퍼 함수 ---

    private suspend fun insertUser(uniqueValue: String): UserEntity = TestDatabaseFactory.dbQuery {
        UserEntity.new {
            role = Role.USER
            provider = SocialLoginProvider.GOOGLE
            providerId = "pid$uniqueValue"
            displayId = "did$uniqueValue"
            name = "honggd"
            profileImageUrl = null
            introduce = "hello"
            isActive = true
            lastLoginAt = TurninDateTime.now()
        }
    }

    private suspend fun insertRefreshToken(userId: Long) {
        TestDatabaseFactory.dbQuery {
            RefreshTokens.insert {
                it[user] = EntityID(userId, Users)
                it[refreshToken] = "token_$userId"
                it[createdAt] = TurninDateTime.now().toOffsetDateTime()
            }
        }
    }

    private suspend fun insertNotification(userId: Long) {
        TestDatabaseFactory.dbQuery {
            Notifications.insert {
                it[Notifications.userId] = EntityID(userId, Users)
                it[notiType] = NotificationType.FRIEND_REQUEST
                it[message] = "test"
                it[isRead] = false
                it[isBroadcast] = false
            }
        }
    }

    private suspend fun insertUserFcmToken(userId: Long) {
        TestDatabaseFactory.dbQuery {
            UserFcmTokens.insert {
                it[UserFcmTokens.userId] = EntityID(userId, Users)
                it[token] = "fcm_token_$userId"
                it[isActive] = true
            }
        }
    }

    private suspend fun insertAnnouncementRead(userId: Long) {
        val announcement = TestDatabaseFactory.dbQuery {
            AnnouncementEntity.new {
                title = "공지사항"
                content = "내용"
                targetAudience = AnnouncementAudience.ALL
                status = AnnouncementStatus.ACTIVE
            }
        }
        TestDatabaseFactory.dbQuery {
            AnnouncementReads.insert {
                it[AnnouncementReads.userId] = EntityID(userId, Users)
                it[announcementId] = announcement.id
                it[readAt] = TurninDateTime.now().toOffsetDateTime()
            }
        }
    }

    private suspend fun insertFriend(requesterId: Long, receiverId: Long) {
        TestDatabaseFactory.dbQuery {
            Friends.insert {
                it[Friends.requesterId] = EntityID(requesterId, Users)
                it[Friends.receiverId] = EntityID(receiverId, Users)
                it[status] = FriendRequestStatus.PENDING
            }
        }
    }

    private suspend fun insertBlock(blockerId: Long, blockedId: Long) {
        TestDatabaseFactory.dbQuery {
            Blocks.insert {
                it[Blocks.blockerId] = EntityID(blockerId, Users)
                it[Blocks.blockedId] = EntityID(blockedId, Users)
                it[reasonId] = EntityID(1L, BlockReasons)
            }
        }
    }

    private suspend fun insertUserKeyword(userId: Long, keywordId: Long) {
        TestDatabaseFactory.dbQuery {
            UserKeywordEntity.new {
                this.userId = EntityID(userId, Users)
                this.keywordId = EntityID(keywordId, Keywords)
                this.isActive = true
            }
        }
    }

    private suspend fun insertReport(reporterId: Long, reportedId: Long) {
        TestDatabaseFactory.dbQuery {
            Reports.insert {
                it[Reports.reporterId] = EntityID(reporterId, Users)
                it[Reports.reportedId] = EntityID(reportedId, Users)
                it[reasonId] = EntityID(1L, ReportReasons)
            }
        }
    }

    private suspend fun insertKeyword(keyword: String): KeywordEntity =
        TestDatabaseFactory.dbQuery {
            KeywordEntity.new {
                this.keyword = keyword
                this.embedding = "test-embedding"
                this.createdBy = null
            }
        }
}
