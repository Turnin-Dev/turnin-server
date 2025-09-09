package com.peekr.domain.auth.infrastructure.repository.impl

import com.peekr.common.db.DatabaseException
import com.peekr.common.util.PeekrDateTime
import com.peekr.domain.auth.AuthTestDoubles.MockRegister
import com.peekr.domain.auth.domain.model.SocialLoginProviderForAuth
import com.peekr.util.TestDatabaseFactory
import junit.framework.TestCase.assertFalse
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
        val savedUser = repository.save(MockRegister)
        assertTrue(savedUser.id > 0L)

        val foundUser = repository.findAuthUserByProviderAndProviderId(
            provider = MockRegister.provider,
            providerId = MockRegister.providerId,
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
        val savedUser = repository.save(MockRegister)

        assertTrue(savedUser.id > 0L)

        val exception = assertFailsWith<DatabaseException.DuplicatedDataException> {
            repository.save(MockRegister) // 동일한 providerId 삽입 시도
        }

        println("발생한 예외: ${exception.message}")
    }

    @Test
    fun `findUserByUserId 성공 테스트`() = runTest {
        // given
        val savedUser = repository.save(MockRegister)
        assertTrue(savedUser.id > 0L)

        // when
        val foundUser = repository.findUserByUserId(savedUser.id)

        // then
        assertNotNull(foundUser)
        assertEquals(savedUser, foundUser)
    }

    @Test
    fun `findUserByUserId 실패 테스트 - 사용자가 존재하지 않는 경우`() = runTest {
        // when
        val foundUser = repository.findUserByUserId(100)

        // then
        assertNull(foundUser)
    }

    @Test
    fun `updateLastLoginAt 성공 테스트`() = runTest {
        // given
        val savedUser = repository.save(MockRegister)
        val userId = savedUser.id

        // when
        val before = PeekrDateTime.now()
        repository.updateLastLoginAt(userId)
        val after = PeekrDateTime.now()

        // then
        val updatedUser = repository.findUserByUserId(userId)
        assertNotNull(updatedUser?.lastLoginAt)
        assertTrue(updatedUser.lastLoginAt in before..after)
    }

    @Test
    fun `updateLastLoginAt 실패 테스트 - 2초 뒤에 시간과 비교`() = runTest {
        // given
        val savedUser = repository.save(MockRegister)
        val userId = savedUser.id

        // when
        repository.updateLastLoginAt(userId)
        val before = PeekrDateTime.now().plusSeconds(1)
        val after = PeekrDateTime.now().plusSeconds(2)

        // then
        val updatedUser = repository.findUserByUserId(userId)
        assertNotNull(updatedUser?.lastLoginAt)
        assertFalse(updatedUser.lastLoginAt in before..after)
    }

    @Test
    fun `existsByDisplayId 성공 테스트`() = runTest {
        // given
        val savedUser = repository.save(MockRegister)
        val displayId = savedUser.displayId

        // when
        val result = repository.existsByDisplayId(displayId)

        // then
        assertTrue(result)
    }

    @Test
    fun `existsByDisplayId 실패 테스트 - 사용자 표시 ID로 찾지 못할 때`() = runTest {
        // when
        val result = repository.existsByDisplayId("a123")

        // then
        assertFalse(result)
    }
}
