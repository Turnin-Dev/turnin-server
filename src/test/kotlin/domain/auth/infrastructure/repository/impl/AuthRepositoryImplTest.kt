package com.peekr.domain.auth.infrastructure.repository.impl

import com.peekr.domain.auth.AuthTestDoubles.MockAuthUser
import com.peekr.domain.auth.domain.model.SocialLoginProviderForAuth
import com.peekr.domain.auth.exception.AuthException
import com.peekr.util.TestDatabaseFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before

class AuthRepositoryImplTest {
    private val repository = AuthRepositoryImpl()

    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @Test
    fun `save & findByProviderAndProviderId 성공 테스트`() = runTest {
        TestDatabaseFactory.init()
        val savedUser = repository.save(MockAuthUser)
        assertTrue(savedUser.id > 0L)

        val foundUser = repository.findAuthUserByProviderAndProviderId(
            provider = MockAuthUser.provider,
            providerId = MockAuthUser.providerId,
        )

        assertNotNull(foundUser)
        assertEquals(savedUser, foundUser)
    }

    @Test
    fun `findByProviderAndProviderId 실패 테스트 - 존재하지 않는 사용자`() = runTest {
        val notFoundUser = repository.findAuthUserByProviderAndProviderId(
            provider = SocialLoginProviderForAuth.KAKAO,
            providerId = "not_found_id",
        )

        assertNull(notFoundUser)
    }

    @Test
    fun `save 실패 테스트 - 중복된 providerId 저장 시도`() = runTest {
        val savedUser = repository.save(MockAuthUser)

        assertTrue(savedUser.id > 0L)

        val exception = assertFailsWith<AuthException.DuplicateUserException> {
            repository.save(MockAuthUser) // 동일한 providerId 삽입 시도
        }

        println("발생한 예외: ${exception.message}")
    }

    @Test
    fun `findUserByDisplayId 성공 테스트`() = runTest {
        // given
        val savedUser = repository.save(MockAuthUser)
        assertTrue(savedUser.id > 0L)

        // when
        val foundedUser = repository.findUserByDisplayId(savedUser.displayId)

        // then
        assertNotNull(foundedUser)
        assertEquals(savedUser, foundedUser)
    }

    @Test
    fun `findUserByDisplayId 실패 테스트 - 사용자가 존재하지 않는 경우`() = runTest {
        // when
        val foundedUser = repository.findUserByDisplayId("not-username")

        // then
        assertNull(foundedUser)
    }
}
