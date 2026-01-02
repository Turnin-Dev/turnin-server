package com.peekr.domain.user.infrastructure.repository.impl

import com.peekr.common.model.Introduce
import com.peekr.common.model.Name
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId
import com.peekr.domain.user.UserTestDoubles
import com.peekr.domain.user.domain.model.UserPatch
import com.peekr.domain.user.domain.repository.UserRepository
import com.peekr.util.TestDatabaseFactory
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
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

    @AfterTest
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `findById 성공 테스트`() = runTest {
        // given
        val savedUserEntity = TestDatabaseFactory.dbQuery {
            UserTestDoubles.saveAndGetUserEntity()
        }

        // when
        val userId = UserId(savedUserEntity.id.value)
        val user = repository.findById(userId)

        // then
        assertNotNull(user)
        assertEquals(userId, user.id)
    }

    @Test
    fun `findByIds 성공 테스트`() = runTest {
        // given: 10명의 테스트 사용자를 생성
        val savedUserId = mutableListOf<UserId>()
        val userTotalCount = 10
        TestDatabaseFactory.dbQuery {
            repeat(userTotalCount) {
                UserTestDoubles.saveAndGetUserEntity("pid$it", "did$it").let {
                    savedUserId.add(UserId(it.id.value))
                }
            }
        }

        // when
        val users = repository.findByIds(savedUserId)

        // then
        assertTrue(users.isNotEmpty())
        assertEquals(userTotalCount, users.size)
        assertEquals(savedUserId, users.map { it.id })
    }

    @Test
    fun `findById 실패 테스트 - 사용자를 찾지 못하는 경우 null를 반환한다`() = runTest {
        // when
        val userEntity = repository.findById(UserId(1L))

        // then
        assertNull(userEntity)
    }

    @Test
    fun `findByDisplayId 성공 테스트`() = runTest {
        // given
        val displayId = TestDatabaseFactory.dbQuery {
            val savedUserEntity = UserTestDoubles.saveAndGetUserEntity()
            DisplayId(savedUserEntity.displayId)
        }

        // when
        val user = repository.findByDisplayId(displayId)

        // then
        assertNotNull(user)
        assertEquals(displayId, user.displayId)
    }

    @Test
    fun `findByDisplayId 실패 테스트 - 사용자를 찾지 못하는 경우 null를 반환한다`() = runTest {
        // when
        val userEntity = repository.findByDisplayId(DisplayId("did"))

        // then
        assertNull(userEntity)
    }

    @Test
    fun `update 실패 테스트 - 사용자를 찾지 못하는 경우 false를 반환한다`() = runTest {
        // when
        val userId = UserId(1L)
        val result = repository.update(userId, TestUserPatch)

        // then
        assertFalse(result)
    }

    @Test
    fun `updateIntroduce 성공 테스트`() = runTest {
        // given
        val savedUserEntity = TestDatabaseFactory.dbQuery {
            UserTestDoubles.saveAndGetUserEntity()
        }

        // when
        val userId = UserId(savedUserEntity.id.value)
        val result = repository.updateIntroduce(userId, TestIntroduce)
        val updatedUserEntity = repository.findById(userId)

        // then
        assertTrue(result)
        assertEquals(TestIntroduce, updatedUserEntity?.introduce)
    }

    @Test
    fun `updateIntroduce 실패 테스트 - 사용자를 찾지 못하는 경우 false를 반환한다`() = runTest {
        // when
        val userId = UserId(1L)
        val result = repository.updateIntroduce(userId, TestIntroduce)

        // then
        assertFalse(result)
    }

    companion object {
        private val TestUserPatch = UserPatch(
            name = Name("name"),
            profileImageUrl = null,
            introduce = Introduce("introduce"),
        )
        private val TestIntroduce = Introduce("introduce")
    }
}
