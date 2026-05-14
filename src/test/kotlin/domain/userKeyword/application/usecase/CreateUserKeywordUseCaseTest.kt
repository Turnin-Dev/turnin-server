package com.turnin.domain.userKeyword.application.usecase

import com.turnin.common.firebase.RefType
import com.turnin.common.model.KeywordName
import com.turnin.common.model.NotificationType
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.userKeyword.application.dto.CreateUserKeywordDto
import com.turnin.domain.userKeyword.application.dto.toDto
import com.turnin.domain.userKeyword.domain.message.UserKeywordNotificationMessage
import com.turnin.domain.userKeyword.domain.model.Description
import com.turnin.domain.userKeyword.domain.model.ExternalKeyword
import com.turnin.domain.userKeyword.domain.model.UserKeyword
import com.turnin.domain.userKeyword.domain.model.UserKeywordFriendFcmContext
import com.turnin.domain.userKeyword.domain.provider.FriendProvider
import com.turnin.domain.userKeyword.domain.provider.KeywordProvider
import com.turnin.domain.userKeyword.domain.provider.NotificationProvider
import com.turnin.domain.userKeyword.domain.repository.UserKeywordRepository
import com.turnin.domain.userKeyword.exception.UserKeywordException
import com.turnin.util.db.TestDatabaseFactory
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateUserKeywordUseCaseTest {
    private val userKeywordRepository = mockk<UserKeywordRepository>()
    private val keywordProvider = mockk<KeywordProvider>()
    private val friendProvider = mockk<FriendProvider>()
    private val notificationProvider = mockk<NotificationProvider>()
    private val applicationScope = TestScope()
    private lateinit var usecase: CreateUserKeywordUseCase

    @Before
    fun setUp() {
        TestDatabaseFactory.init()

        coEvery { userKeywordRepository.countByUserId(TestUserId) } returns 3
        coEvery {
            userKeywordRepository.create(
                keywordId = TestUserKeyword.keywordId,
                userId = TestUserKeyword.userId,
                description = TestDescription,
            )
        } returns TestUserKeyword
        coEvery {
            friendProvider.getFriendFcmContext(TestUserId)
        } returns TestUserKeywordFriendFcmContext
        coEvery {
            notificationProvider.sendNotificationToTokens(any(), any(), any(), any(), any(), any())
        } just Runs

        usecase = CreateUserKeywordUseCase(
            userKeywordRepository,
            keywordProvider,
            notificationProvider,
            friendProvider,
            applicationScope,
        )
    }

    @After
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `이미 키워드가 존재하는 경우 해당 키워드 ID로 사용자 키워드를 저장한다`() = runTest {
        // given
        coEvery { keywordProvider.findByName(any()) } returns TestExternalKeyword

        // when
        val userKeyword = usecase(TestCreateUserKeywordDto)

        // then
        assertEquals(userKeyword, TestUserKeyword.toDto(TestKeywordName.value))
    }

    @Test
    fun `키워드가 존재하지 않는 경우 저장하고 저장된 키워드 ID로 사용자 키워드를 저장한다`() = runTest {
        // given
        coEvery { keywordProvider.findByName(TestKeywordName.value) } returns null
        coEvery { keywordProvider.create(TestKeywordName.value, TestUserId) } returns TestExternalKeyword

        // when
        val userKeyword = usecase(TestCreateUserKeywordDto)

        // then
        assertEquals(userKeyword, TestUserKeyword.toDto(TestKeywordName.value))
    }

    @Test
    fun `사용자 키워드 개수 제한 도달 시 예외가 발생한다`() = runTest {
        // given
        coEvery {
            userKeywordRepository.countByUserId(TestUserId)
        } returns UserKeyword.COUNT_LIMIT + 1L

        // when
        val exception = runCatching {
            usecase(TestCreateUserKeywordDto)
        }.exceptionOrNull()

        // then
        assertTrue(exception is UserKeywordException.CountLimitReached)
    }

    @Test
    fun `키워드 생성 후 친구들에게 알림을 전송한다`() = runTest {
        // given
        coEvery { keywordProvider.findByName(any()) } returns TestExternalKeyword

        // when
        usecase(TestCreateUserKeywordDto)
        applicationScope.advanceUntilIdle() // 비동기 작업 완료 대기

        // then
        coVerify(exactly = 1) {
            notificationProvider.sendNotificationToTokens(
                tokens = TestUserKeywordFriendFcmContext.friendTokens,
                notiType = NotificationType.NEW_KEYWORD,
                title = UserKeywordNotificationMessage.TITLE,
                message = UserKeywordNotificationMessage.message(TestUserKeywordFriendFcmContext.senderName),
                refId = TestUserKeyword.id.value,
                refType = RefType.KEYWORD,
                senderUserId = TestUserId.value,
            )
        }
    }

    @Test
    fun `친구가 없으면 알림을 전송하지 않는다`() = runTest {
        // given
        coEvery { keywordProvider.findByName(any()) } returns TestExternalKeyword
        coEvery {
            friendProvider.getFriendFcmContext(TestUserId)
        } returns TestUserKeywordFriendFcmContext.copy(friendTokens = emptyList())

        // when
        usecase(TestCreateUserKeywordDto)
        applicationScope.advanceUntilIdle()

        // then
        coVerify(exactly = 0) {
            notificationProvider.sendNotificationToTokens(any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `알림 전송 실패 시에도 키워드 생성은 성공으로 처리한다`() = runTest {
        // given
        coEvery { keywordProvider.findByName(any()) } returns TestExternalKeyword
        coEvery {
            notificationProvider.sendNotificationToTokens(any(), any(), any(), any(), any(), any())
        } throws RuntimeException("알림 전송 실패")

        // when
        val result = runCatching {
            usecase(TestCreateUserKeywordDto)
            applicationScope.advanceUntilIdle()
        }

        // then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `사용자 키워드 개수 제한 도달 시 알림을 전송하지 않는다`() = runTest {
        // given
        coEvery {
            userKeywordRepository.countByUserId(TestUserId)
        } returns UserKeyword.COUNT_LIMIT + 1L

        // when
        runCatching { usecase(TestCreateUserKeywordDto) }
        applicationScope.advanceUntilIdle()

        // then
        coVerify(exactly = 0) {
            notificationProvider.sendNotificationToTokens(any(), any(), any(), any(), any(), any())
        }
    }

    companion object {
        private val TestUserId = UserId(1)
        private val TestKeywordId = KeywordId(1)
        private val TestUserKeywordId = UserKeywordId(1)
        private val TestKeywordName = KeywordName("keyword")
        private val TestDescription = Description("test")
        private val TestUserKeyword = UserKeyword(
            id = TestUserKeywordId,
            userId = TestUserId,
            keywordId = TestKeywordId,
            description = TestDescription,
            createdAt = 1000,
            updatedAt = 1000,
        )
        private val TestCreateUserKeywordDto = CreateUserKeywordDto(
            userId = TestUserKeyword.userId,
            keywordName = TestKeywordName.value,
            description = TestDescription.toDto(),
        )
        private val TestExternalKeyword = ExternalKeyword(
            id = TestKeywordId,
            name = TestKeywordName,
            createdBy = TestUserId,
            createdAt = 1000,
            updatedAt = 1000,
        )
        private val TestUserKeywordFriendFcmContext = UserKeywordFriendFcmContext(
            friendTokens = listOf("token1", "token2", "token3"),
            senderName = "테스트유저",
        )
    }
}
