package com.peekr.common.db.extension

import com.peekr.common.db.schema.UserEntity
import com.peekr.common.db.schema.Users
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.id.UserId
import com.peekr.util.db.TestDatabaseFactory
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
