package com.peekr.domain.user.application.usecase

import com.peekr.domain.user.UserTestDoubles
import com.peekr.domain.user.application.dto.toDto
import com.peekr.domain.user.domain.service.UserService
import com.peekr.domain.user.infrastructure.mapper.UserMapper
import com.peekr.util.TestDatabaseFactory
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Before

class UserUseCaseImplTest {
    private val userService = mockk<UserService>()
    private val usecase = UserUseCaseImpl(userService)

    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @Test
    fun `getUserById 성공 테스트`() = runTest {
        // given
        val expectedUser = TestDatabaseFactory.dbQuery {
            val entity = UserTestDoubles.getUserEntity()
            UserMapper.toDomain(entity)
        }
        coEvery { userService.getUserById(any()) } returns expectedUser

        // when
        val userDto = usecase.getUserById(expectedUser.id)

        // then
        assertNotNull(userDto)
        assertEquals(expectedUser.toDto(), userDto)
    }

    @Test
    fun `getUserById 실패 테스트 - 사용자가 존재하지 않을 때`() = runTest {
        // given
        coEvery { userService.getUserById(any()) } returns null

        // when
        val userDto = usecase.getUserById(1L)

        // then
        assertNull(userDto)
    }
}
