package com.peekr.domain.keywordGraph.application.usecase

import com.peekr.common.model.Introduce
import com.peekr.common.model.KeywordName
import com.peekr.common.model.Name
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.common.util.pagination.cursor.CursorPage
import com.peekr.domain.keywordGraph.domain.model.SharedKeywordInfo
import com.peekr.domain.keywordGraph.domain.provider.ExternalKeyword
import com.peekr.domain.keywordGraph.domain.provider.ExternalUser
import com.peekr.domain.keywordGraph.domain.provider.KeywordProvider
import com.peekr.domain.keywordGraph.domain.provider.UserProvider
import com.peekr.domain.keywordGraph.domain.repository.KeywordGraphRepository
import com.peekr.domain.keywordGraph.exception.KeywordGraphException
import com.peekr.util.TestDatabaseFactory
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class GetNodeContextUseCaseTest {
    private val keywordGraphRepository: KeywordGraphRepository = mockk()
    private val userProvider: UserProvider = mockk()
    private val keywordProvider: KeywordProvider = mockk()
    private val usecase = GetNodeContextUseCase(keywordGraphRepository, userProvider, keywordProvider)

    @Before
    fun setUp() {
        TestDatabaseFactory.init()

        // Mock Data
    }

    @After
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `NodeContext 페이지네이션 조회 성공 테스트`() = runTest {
        // given
        val userKeywordIds = createUserKeywordIds(2)
        val keywordIds = createKeywordIds(2)
        val sharedKeywordInfos = List(PAGE_SIZE) {
            createSharedKeywordInfo(
                userId = UserId((it + 1).toLong()),
                userKeywordIds = userKeywordIds,
                keywordIds = keywordIds,
            )
        }
        val cursorPage = CursorPage(sharedKeywordInfos, null)
        val users = sharedKeywordInfos.map { createExternalUser(it.userId) }
        val keywords = keywordIds.map { createExternalKeyword(it) }

        coEvery {
            keywordGraphRepository.getSharedKeywordInfos(TestUserId, any(), any())
        } returns cursorPage
        coEvery {
            userProvider.findByIds(any())
        } returns users
        coEvery {
            keywordProvider.findByIds(any())
        } returns keywords

        // when
        val actualCursorPage = usecase(TestUserId.value, 1L, PAGE_SIZE)

        // then
        assertEquals(PAGE_SIZE, actualCursorPage.items.size)
        assertEquals(
            sharedKeywordInfos.map { it.userId },
            actualCursorPage.items.map { it.userNodeDto.userId },
        )
        assertEquals(
            sharedKeywordInfos.map { it.userKeywordIds },
            actualCursorPage.items.map {
                it.keywordNodeDtoList.map { it2 ->
                    it2.userKeywordId
                }
            },
        )
    }

    @Test
    fun `사용자 키워드 ID 리스트 사이즈와 키워드 ID 리스트 사이즈가 다른 경우 예외가 발생한다`() = runTest {
        // given: userKeywordIds와 keywordIds 사이즈가 다르도록 구성
        val userKeywordIds = createUserKeywordIds(2)
        val keywordIds = createKeywordIds(4)
        val sharedKeywordInfos = List(PAGE_SIZE) {
            createSharedKeywordInfo(
                userId = UserId((it + 1).toLong()),
                userKeywordIds = userKeywordIds,
                keywordIds = keywordIds,
            )
        }
        val cursorPage = CursorPage(sharedKeywordInfos, null)
        val users = sharedKeywordInfos.map { createExternalUser(it.userId) }
        val keywords = keywordIds.map { createExternalKeyword(it) }

        coEvery {
            keywordGraphRepository.getSharedKeywordInfos(TestUserId, any(), any())
        } returns cursorPage
        coEvery {
            userProvider.findByIds(any())
        } returns users
        coEvery {
            keywordProvider.findByIds(any())
        } returns keywords

        // when
        val exception = runCatching {
            usecase(TestUserId.value, 1L, PAGE_SIZE)
        }.exceptionOrNull()

        // then
        assertNotNull(exception)
        assertTrue(exception is KeywordGraphException.KeywordIdPairingFailed)
    }

    @Test
    fun `사용자 Map 에서 사용자를 찾지 못하는 경우 예외가 발생한다`() = runTest {
        // given: 사용자 정보를 조회할 때 빈 리스트를 반환하도록 구성
        val userKeywordIds = createUserKeywordIds(2)
        val keywordIds = createKeywordIds(2)
        val sharedKeywordInfos = List(PAGE_SIZE) {
            createSharedKeywordInfo(
                userId = UserId((it + 1).toLong()),
                userKeywordIds = userKeywordIds,
                keywordIds = keywordIds,
            )
        }
        val cursorPage = CursorPage(sharedKeywordInfos, null)
        val keywords = keywordIds.map { createExternalKeyword(it) }

        coEvery {
            keywordGraphRepository.getSharedKeywordInfos(TestUserId, any(), any())
        } returns cursorPage
        coEvery {
            userProvider.findByIds(any())
        } returns emptyList()
        coEvery {
            keywordProvider.findByIds(any())
        } returns keywords

        // when
        val exception = runCatching {
            usecase(TestUserId.value, 1L, PAGE_SIZE)
        }.exceptionOrNull()

        // then
        assertNotNull(exception)
        assertTrue(exception is KeywordGraphException.UserNotFound)
    }

    @Test
    fun `키워드 Map 에서 키워드를 찾지 못하는 경우 예외가 발생한다`() = runTest {
        // given: 키워드 정보를 조회할 때 빈 리스트를 반환하도록 구성
        val userKeywordIds = createUserKeywordIds(2)
        val keywordIds = createKeywordIds(2)
        val sharedKeywordInfos = List(PAGE_SIZE) {
            createSharedKeywordInfo(
                userId = UserId((it + 1).toLong()),
                userKeywordIds = userKeywordIds,
                keywordIds = keywordIds,
            )
        }
        val cursorPage = CursorPage(sharedKeywordInfos, null)
        val users = sharedKeywordInfos.map { createExternalUser(it.userId) }

        coEvery {
            keywordGraphRepository.getSharedKeywordInfos(TestUserId, any(), any())
        } returns cursorPage
        coEvery {
            userProvider.findByIds(any())
        } returns users
        coEvery {
            keywordProvider.findByIds(any())
        } returns emptyList()

        // when
        val exception = runCatching {
            usecase(TestUserId.value, 1L, PAGE_SIZE)
        }.exceptionOrNull()

        // then
        assertNotNull(exception)
        assertTrue(exception is KeywordGraphException.KeywordIdPairingFailed)
    }

    companion object {
        private const val PAGE_SIZE = 5

        private val TestUserId = UserId(1)

        private fun createUserKeywordIds(count: Int) = List(count) { UserKeywordId((it + 1).toLong()) }

        private fun createKeywordIds(count: Int) = List(count) { KeywordId((it + 1).toLong()) }

        private fun createSharedKeywordInfo(
            userId: UserId,
            userKeywordIds: List<UserKeywordId>,
            keywordIds: List<KeywordId>,
        ) = SharedKeywordInfo(
            userId = userId,
            userKeywordIds = userKeywordIds,
            keywordIds = keywordIds,
        )

        private fun createExternalUser(userId: UserId) =
            ExternalUser(
                id = userId,
                role = Role.USER,
                provider = SocialLoginProvider.GOOGLE,
                providerId = "providerId",
                displayId = DisplayId("displayId"),
                name = Name("name"),
                profileImageUrl = "profileImageUrl",
                introduce = Introduce("introduce"),
                isActive = true,
                lastLoginAt = 1000L,
            )

        private fun createExternalKeyword(keywordId: KeywordId) = ExternalKeyword(
            id = keywordId,
            name = KeywordName("keywordName"),
            createdBy = UserId(1L),
            createdAt = 1000L,
            updatedAt = 1000L,
        )
    }
}
