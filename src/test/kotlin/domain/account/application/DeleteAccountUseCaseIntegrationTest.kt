package com.turnin.domain.account.application

import com.turnin.common.db.schema.BlockEntity
import com.turnin.common.db.schema.BlockReasons
import com.turnin.common.db.schema.Blocks
import com.turnin.common.db.schema.FriendEntity
import com.turnin.common.db.schema.Friends
import com.turnin.common.db.schema.KeywordEntity
import com.turnin.common.db.schema.NotificationEntity
import com.turnin.common.db.schema.Notifications
import com.turnin.common.db.schema.RefreshTokens
import com.turnin.common.db.schema.UserEntity
import com.turnin.common.db.schema.UserKeywordEntity
import com.turnin.common.db.schema.UserKeywords
import com.turnin.common.db.schema.Users
import com.turnin.common.model.FriendRequestStatus
import com.turnin.common.model.NotificationType
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.domain.account.exception.AccountException
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

        // user_keyword 비활성화 검증
        val userKeywords = findUserKeywordsByUserIdForTest(user.id.value)
        assertTrue(userKeywords.all { !it.isActive })

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
        coVerify(exactly = 1) { mockFileDeletionSupportApi.deleteFile("https://r2.example.com/profile.jpg") }

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
        coVerify(exactly = 1) { mockFileDeletionSupportApi.deleteFile("https://r2.example.com/profile.jpg") }
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
        coVerify(exactly = 0) { mockFileDeletionSupportApi.deleteFile(any()) }
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
