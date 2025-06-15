package com.peekr.domain.auth.infrastructure.repositoryImpl

import com.peekr.domain.auth.AuthTestDoubles.MockAuthUser
import com.peekr.domain.auth.domain.model.value.SocialLoginProvider
import com.peekr.util.TestDatabaseFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.exceptions.ExposedSQLException
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

        val foundUser = repository.findByProviderAndProviderId(
            provider = MockAuthUser.provider,
            providerId = MockAuthUser.providerId,
        )

        assertNotNull(foundUser)
        assertEquals(savedUser, foundUser)
    }

    @Test
    fun `findByProviderAndProviderId 실패 테스트 - 존재하지 않는 사용자`() = runTest {
        val notFoundUser = repository.findByProviderAndProviderId(
            provider = SocialLoginProvider.Kakao,
            providerId = "not_found_id",
        )

        assertNull(notFoundUser)
    }

    @Test
    fun `save 실패 테스트 - 중복된 providerId 저장 시도`() = runTest {
        val savedUser = repository.save(MockAuthUser)

        assertTrue(savedUser.id > 0L)

        val exception = assertFailsWith<ExposedSQLException> {
            repository.save(MockAuthUser) // 동일한 providerId 삽입 시도
        }

        println("발생한 예외: ${exception.message}")
    }
}
