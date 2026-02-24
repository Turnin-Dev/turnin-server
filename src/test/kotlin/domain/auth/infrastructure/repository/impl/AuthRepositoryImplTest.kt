package com.peekr.domain.auth.infrastructure.repository.impl

import com.peekr.common.db.DatabaseException
import com.peekr.common.model.Introduce
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.UserName
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId
import com.peekr.common.util.PeekrDateTime
import com.peekr.domain.auth.domain.model.Register
import com.peekr.util.db.TestDatabaseFactory
import com.peekr.util.db.setUserInactiveForTest
import junit.framework.TestCase.assertFalse
import kotlin.test.AfterTest
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

    @AfterTest
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `save & findByProviderAndProviderId 성공 테스트`() = runTest {
        val savedUser = repository.save(TestRegister)
        assertTrue(savedUser.userId.value > 0L)

        val foundUser = repository.findAuthUserByProviderAndProviderId(
            provider = TestRegister.provider,
            providerId = TestRegister.providerId,
        )

        assertNotNull(foundUser)
        assertEquals(savedUser, foundUser)
    }

    @Test
    fun `findByProviderAndProviderId 성공 테스트 - 비활성화 사용자는 조회되지 않는다`() = runTest {
        // given: 사용자 생성 후 비활성화
        val savedUser = repository.save(TestRegister)
        assertTrue(savedUser.userId.value > 0L)
        setUserInactiveForTest(savedUser.userId)

        // when: 비활성화 사용자 조회
        val foundUser = repository.findAuthUserByProviderAndProviderId(
            provider = TestRegister.provider,
            providerId = TestRegister.providerId,
        )

        // then: 사용자는 조회되지 않는다.
        assertNull(foundUser)
    }

    @Test
    fun `findByProviderAndProviderId 실패 테스트 - 존재하지 않는 사용자`() = runTest {
        val notFoundUser = repository.findAuthUserByProviderAndProviderId(
            provider = SocialLoginProvider.KAKAO,
            providerId = "not_found_id",
        )

        assertNull(notFoundUser)
    }

    @Test
    fun `save 실패 테스트 - 중복된 providerId 저장 시도`() = runTest {
        val savedUser = repository.save(TestRegister)

        assertTrue(savedUser.userId.value > 0L)

        val exception = assertFailsWith<DatabaseException.DuplicatedDataException> {
            repository.save(TestRegister) // 동일한 providerId 삽입 시도
        }

        println("발생한 예외: ${exception.message}")
    }

    @Test
    fun `findUserByUserId 성공 테스트`() = runTest {
        // given
        val savedUser = repository.save(TestRegister)
        assertTrue(savedUser.userId.value > 0L)

        // when
        val foundUser = repository.findUserByUserId(savedUser.userId)

        // then
        assertNotNull(foundUser)
        assertEquals(savedUser, foundUser)
    }

    @Test
    fun `findUserByUserId 성공 테스트 - 비활성화 사용자는 조회되지 않는다`() = runTest {
        // given: 사용자 생성 후 비활성화
        val savedUser = repository.save(TestRegister)
        assertTrue(savedUser.userId.value > 0L)
        setUserInactiveForTest(savedUser.userId)

        // when: 비활성화 사용자 조회
        val foundUser = repository.findUserByUserId(savedUser.userId)

        // then: 사용자가 조회되지 않는다.
        assertNull(foundUser)
    }

    @Test
    fun `findUserByUserId 실패 테스트 - 사용자가 존재하지 않는 경우`() = runTest {
        // when
        val foundUser = repository.findUserByUserId(UserId(100))

        // then
        assertNull(foundUser)
    }

    @Test
    fun `updateLastLoginAt 성공 테스트`() = runTest {
        // given
        val savedUser = repository.save(TestRegister)
        val userId = savedUser.userId

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
        val savedUser = repository.save(TestRegister)
        val userId = savedUser.userId

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
        val savedUser = repository.save(TestRegister)
        val displayId = savedUser.displayId

        // when
        val result = repository.existsByDisplayId(displayId)

        // then
        assertTrue(result)
    }

    @Test
    fun `existsByDisplayId 성공 테스트 - 비활성화 사용자도 조회 가능하다`() = runTest {
        // given: 사용자 생성 후 비활성화
        val savedUser = repository.save(TestRegister)
        val displayId = savedUser.displayId
        setUserInactiveForTest(savedUser.userId)

        // when: 비활성화 사용자 조회
        val result = repository.existsByDisplayId(displayId)

        // then: 사용자가 조회된다.
        assertTrue(result)
    }

    @Test
    fun `existsByDisplayId 실패 테스트 - 사용자 표시 ID로 찾지 못할 때`() = runTest {
        // when
        val result = repository.existsByDisplayId(DisplayId("a123"))

        // then
        assertFalse(result)
    }

    companion object {
        private val TestRegister = Register(
            provider = SocialLoginProvider.GOOGLE,
            providerId = "providerIDDDDD",
            displayId = DisplayId("hong_gd_123"),
            userName = UserName("honggd"),
            profileImageUrl = "http://example.com/profile.jpg",
            introduce = Introduce("Hello!"),
        )
    }
}
