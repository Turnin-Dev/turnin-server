package com.turnin.common.db.extension

import com.turnin.common.db.schema.UserEntity
import com.turnin.common.db.schema.Users
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.id.UserId
import com.turnin.util.db.TestDatabaseFactory
import com.turnin.util.db.setUserInactiveForTest
import java.time.Instant
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class UsersExtensionTest {
    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @After
    fun teardown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `existsUser 성공 테스트 - 사용자가 존재하는 경우 true를 반환한다`() = runTest {
        // given
        val userId = insertUserAndReturnId("a")

        // when
        val result = TestDatabaseFactory.dbQuery {
            Users.existsUser(userId)
        }

        // then
        assertTrue(result)
    }

    @Test
    fun `existsUser 성공 테스트 - 비활성화 사용자는 조회되지 않는다`() = runTest {
        // given: 비활성화 사용자 생성
        val userId = insertUserAndReturnId("a")
        setUserInactiveForTest(userId)

        // when
        val result = TestDatabaseFactory.dbQuery {
            Users.existsUser(userId)
        }

        // then: 사용자가 존재하지 않는다.
        assertFalse(result)
    }

    @Test
    fun `existsUser 성공 테스트 - 사용자가 존재하지 않는 경우 false를 반환한다`() = runTest {
        // when
        val result = TestDatabaseFactory.dbQuery {
            Users.existsUser(UserId(100))
        }

        // then
        assertFalse(result)
    }

    private suspend fun insertUserAndReturnId(uniqueValue: String): UserId = TestDatabaseFactory.dbQuery {
        val savedUser = UserEntity.new {
            this.role = Role.USER
            this.provider = SocialLoginProvider.GOOGLE
            this.providerId = "pid$uniqueValue"
            this.displayId = "did$uniqueValue"
            this.name = "honggd"
            this.profileImageUrl = null
            this.introduce = "hello"
            this.isActive = true
            this.lastLoginAt = Instant.now()
        }

        UserId(savedUser.id.value)
    }
}
