package com.peekr.domain.user.infrastructure.service.impl

import com.peekr.domain.user.UserTestDoubles
import com.peekr.domain.user.domain.repository.UserRepository
import com.peekr.domain.user.infrastructure.mapper.UserMapper
import com.peekr.util.TestDatabaseFactory
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.jupiter.api.assertNull

class UserServiceImplTest {
    private val repository = mockk<UserRepository>()
    private val service = UserServiceImpl(repository)

    @Before
    fun setup() {
        TestDatabaseFactory.init()
    }

    @Test
    fun `getUserById 성공 테스트`() = runBlocking {
        // given
        val mappedUser = TestDatabaseFactory.dbQuery {
            val userEntity = UserTestDoubles.getUserEntity()
            UserMapper.toDomain(userEntity)
        }
        coEvery { repository.getUserById(any()) } returns mappedUser

        // when
        val user = service.getUserById(mappedUser.id)

        // then
        assertNotNull(user)
        assertEquals(mappedUser.id, user.id)
    }

    @Test
    fun `getUserById 실패 테스트 - 사용자가 존재하지 않는 경우`() = runBlocking {
        // given
        coEvery { repository.getUserById(any()) } returns null

        // when
        val user = service.getUserById(1L)

        // then
        assertNull(user)
    }
}
