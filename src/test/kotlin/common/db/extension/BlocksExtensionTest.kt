package com.turnin.common.db.extension

import com.turnin.common.db.schema.BlockEntity
import com.turnin.common.db.schema.BlockReasons
import com.turnin.common.db.schema.Blocks
import com.turnin.common.db.schema.UserEntity
import com.turnin.common.db.schema.Users
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.id.UserId
import com.turnin.util.db.TestDatabaseFactory
import java.time.Instant
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.dao.id.EntityID
import org.junit.After
import org.junit.Before
import org.junit.Test

class BlocksExtensionTest {
    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @After
    fun teardown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `차단 여부 확인 성공 테스트 - 차단 관계인 경우`() = runTest {
        // given: 사용자 2명 생성 후 차단 생성
        val userId1 = insertUserAndReturnId("1")
        val userId2 = insertUserAndReturnId("2")
        createBlock(
            blockerId = userId1.value,
            blockedId = userId2.value,
            reasonId = 1L,
            customReason = "custom-reason",
        )

        // when
        val result = TestDatabaseFactory.dbQuery {
            Blocks.isBlockedRelationship(userId1, userId2)
        }

        // then
        assertTrue(result)
    }

    @Test
    fun `차단 여부 확인 성공 테스트 - 차단 관계가 아닌 경우`() = runTest {
        // given: 사용자 2명 생성 후 차단 생성
        val userId1 = insertUserAndReturnId("1")
        val userId2 = insertUserAndReturnId("2")

        // when
        val result = TestDatabaseFactory.dbQuery {
            Blocks.isBlockedRelationship(userId1, userId2)
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

    private suspend fun createBlock(
        blockerId: Long,
        blockedId: Long,
        reasonId: Long,
        customReason: String?,
    ): BlockEntity = TestDatabaseFactory.dbQuery {
        BlockEntity.new {
            this.blockerId = EntityID(blockerId, Users)
            this.blockedId = EntityID(blockedId, Users)
            this.reasonId = EntityID(reasonId, BlockReasons)
            this.customReason = customReason
        }
    }
}
