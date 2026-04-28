package com.turnin.common.db

import com.turnin.common.db.schema.Users
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.util.PeekrDateTime
import com.turnin.common.util.toOffsetDateTime
import com.turnin.util.db.TestDatabaseFactory
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateUpsertWithTimestampTest {
    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @After
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
    }

    private suspend fun insertTestUser(
        providerId: String = "test_provider_id",
        displayId: String = "test_display_id",
        name: String = "테스트유저",
        now: OffsetDateTime = PeekrDateTime.now().toOffsetDateTime(),
    ) = TestDatabaseFactory.dbQuery {
        Users.insertAndGetId { stmt ->
            stmt[Users.role] = Role.USER
            stmt[Users.provider] = SocialLoginProvider.KAKAO
            stmt[Users.providerId] = providerId
            stmt[Users.displayId] = displayId
            stmt[Users.name] = name
            stmt[Users.profileImageUrl] = null
            stmt[Users.introduce] = "안녕하세요"
            stmt[Users.isActive] = true
            stmt[Users.lastLoginAt] = now.toInstant()
            stmt[Users.createdAt] = now
            stmt[Users.updatedAt] = now
        }
    }

    // ===== updateWithTimestamp =====

    @Test
    fun `updateWithTimestamp - UPDATE 시 updated_at이 자동으로 갱신된다`() = runTest {
        // given
        val now = PeekrDateTime.now().toOffsetDateTime()
        val userId = insertTestUser(now = now)
        Thread.sleep(10)

        // when
        TestDatabaseFactory.dbQuery {
            Users.updateWithTimestamp({ Users.id eq userId }) {
                it[name] = "변경된이름"
            }
        }

        // then
        val updatedUser = TestDatabaseFactory.dbQuery {
            Users.selectAll().where { Users.id eq userId }.single()
        }

        assertEquals("변경된이름", updatedUser[Users.name])
        assertTrue(updatedUser[Users.updatedAt].isAfter(now))
    }

    @Test
    fun `updateWithTimestamp - updated_at을 명시적으로 지정하면 해당 값으로 갱신된다`() = runTest {
        // given
        val now = PeekrDateTime.now().toOffsetDateTime()
        val specificTime = now.minusDays(1)
        val userId = insertTestUser(now = now)

        // when
        TestDatabaseFactory.dbQuery {
            Users.updateWithTimestamp({ Users.id eq userId }) {
                it[name] = "변경된이름"
                it[updatedAt] = specificTime
            }
        }

        // then
        val updatedUser = TestDatabaseFactory.dbQuery {
            Users.selectAll().where { Users.id eq userId }.single()
        }

        // 명시적으로 지정한 specificTime이 그대로 유지되는지 검증
        assertTrue(updatedUser[Users.updatedAt].isBefore(now))
        assertEquals(
            specificTime.truncatedTo(ChronoUnit.SECONDS),
            updatedUser[Users.updatedAt].truncatedTo(ChronoUnit.SECONDS),
        )
    }

    // ===== upsertWithTimestamp =====

    @Test
    fun `upsertWithTimestamp - 존재하지 않는 경우 INSERT된다`() = runTest {
        // given
        val now = PeekrDateTime.now().toOffsetDateTime()

        // when
        TestDatabaseFactory.dbQuery {
            Users.upsertWithTimestamp(Users.providerId) {
                it[role] = Role.USER
                it[provider] = SocialLoginProvider.KAKAO
                it[providerId] = "new_provider_id"
                it[displayId] = "new_display_id"
                it[name] = "새유저"
                it[profileImageUrl] = null
                it[introduce] = "안녕하세요"
                it[isActive] = true
                it[lastLoginAt] = now.toInstant()
                it[createdAt] = now
                // updatedAt은 함수 내부에서 자동 갱신
            }
        }

        // then
        val insertedUser = TestDatabaseFactory.dbQuery {
            Users.selectAll().where { Users.providerId eq "new_provider_id" }.single()
        }

        assertEquals("새유저", insertedUser[Users.name])
        assertNotNull(insertedUser[Users.updatedAt])
    }

    @Test
    fun `upsertWithTimestamp - 이미 존재하는 경우 UPDATE되고 updated_at이 자동으로 갱신된다`() = runTest {
        // given
        val now = PeekrDateTime.now().toOffsetDateTime()
        insertTestUser(providerId = "existing_provider_id", now = now)
        Thread.sleep(10)

        // when
        TestDatabaseFactory.dbQuery {
            Users.upsertWithTimestamp(Users.provider, Users.providerId) {
                it[role] = Role.USER
                it[provider] = SocialLoginProvider.KAKAO
                it[providerId] = "existing_provider_id"
                it[displayId] = "test_display_id"
                it[name] = "변경된이름"
                it[profileImageUrl] = null
                it[introduce] = "안녕하세요"
                it[isActive] = true
                it[lastLoginAt] = now.toInstant()
                it[createdAt] = now
                // updatedAt은 함수 내부에서 자동 갱신
            }
        }

        // then
        val upsertedUser = TestDatabaseFactory.dbQuery {
            Users.selectAll().where { Users.providerId eq "existing_provider_id" }.single()
        }

        assertEquals("변경된이름", upsertedUser[Users.name])
        assertTrue(upsertedUser[Users.updatedAt].isAfter(now))
    }
}
