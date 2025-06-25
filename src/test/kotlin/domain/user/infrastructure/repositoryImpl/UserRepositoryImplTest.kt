package com.peekr.domain.user.infrastructure.repositoryImpl

import com.peekr.domain.user.UserTestDoubles
import com.peekr.domain.user.domain.repository.UserRepository
import com.peekr.util.TestDatabaseFactory
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull

class UserRepositoryImplTest {
    private val repository: UserRepository = UserRepositoryImpl()

    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @Test
    fun `getUserById 성공 테스트`() = runTest {
        // given
        val savedUserEntity = TestDatabaseFactory.dbQuery {
            UserTestDoubles.getUserEntity()
        }

        // when
        val user = repository.getUserById(savedUserEntity.id.value)

        // then
        assertNotNull(user)
        assertEquals(savedUserEntity.id.value, user.id)
    }

    @Test
    fun `getUserById 실패 테스트 - 사용자를 찾지 못하는 경우`() = runTest {
        // when
        val userEntity = repository.getUserById(1L)

        // then
        assertNull(userEntity)
    }
}
