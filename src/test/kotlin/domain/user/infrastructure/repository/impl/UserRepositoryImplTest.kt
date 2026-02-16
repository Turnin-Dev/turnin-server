package com.peekr.domain.user.infrastructure.repository.impl

import com.peekr.common.db.schema.BlockEntity
import com.peekr.common.db.schema.BlockReasons
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.db.schema.Users
import com.peekr.common.model.Introduce
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.UserName
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId
import com.peekr.domain.user.domain.model.User
import com.peekr.domain.user.domain.model.UserPatch
import com.peekr.domain.user.domain.repository.UserRepository
import com.peekr.domain.user.infrastructure.mapper.UserMapper.toDomain
import com.peekr.util.db.TestDatabaseFactory
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.dao.id.EntityID
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
        val user = insertUser("1")

        // when
        val userResult = repository.findById(user.id)

        // then
        assertNotNull(userResult)
        assertEquals(user, userResult)
    }

    @Test
    fun `findVisibleById 성공 테스트`() = runTest {
        // given
        val currentUser = insertUser("current")
        val user = insertUser("1")
        val currentId = UserId(currentUser.id.value)
        val userId = UserId(user.id.value)

        // when
        val userResult = repository.findVisibleById(currentId, userId)

        // then
        assertNotNull(userResult)
        assertEquals(userId, userResult.id)
    }

    @Test
    fun `findVisibleById 성공 테스트 - 내가 차단한 사용자를 조회 시 isBlocked가 true인 채로 조회된다`() = runTest {
        // given: 사용자 2명 생성 후 차단 관계 설정
        val me = insertUser("1")
        val user = insertUser("2")
        // me -> user 차단
        createBlock(me.id.value, user.id.value)

        // when: 사용자 조회
        val myResult = repository.findVisibleById(me.id, user.id)

        // then: isBlocked가 true인 채로 조회된다.
        assertNotNull(myResult)
        assertTrue(myResult.isBlocked)
    }

    @Test
    fun `findVisibleById 성공 테스트 - 내가 차단 당한 사용자를 조회 시 조회되지 않는다`() = runTest {
        // given: 사용자 2명 생성 후 차단 관계 설정
        val me = insertUser("1")
        val user = insertUser("2")
        // user -> me 차단
        createBlock(user.id.value, me.id.value)

        // when: 차단 당한 사용자 조회
        val myResult = repository.findVisibleById(me.id, user.id)

        // then: 사용자가 조회되지 않는다.
        assertNull(myResult)
    }

    @Test
    fun `findVisibleById 성공 테스트 - 상호 차단인 경우 서로 조회되지 않는다`() = runTest {
        // given: 사용자 2명 생성 후 차단 관계 설정
        val me = insertUser("1")
        val user = insertUser("2")
        // 상호 차단
        createBlock(me.id.value, user.id.value)
        createBlock(user.id.value, me.id.value)

        // when: 사용자 서로 조회
        val myResult = repository.findVisibleById(me.id, user.id)
        val userResult = repository.findVisibleById(user.id, me.id)

        // then: 사용자가 서로 조회되지 않는다.
        assertNull(myResult)
        assertNull(userResult)
    }

    @Test
    fun `findVisibleById 실패 테스트 - 사용자를 찾지 못하는 경우 null를 반환한다`() = runTest {
        // when
        val userEntity = repository.findVisibleById(UserId(1), UserId(1L))

        // then
        assertNull(userEntity)
    }

    @Test
    fun `findByIds 성공 테스트`() = runTest {
        // given: 10명의 테스트 사용자를 생성
        val savedUserId = mutableListOf<UserId>()
        val userTotalCount = 10
        repeat(userTotalCount) {
            val userId = insertUser("${it + 1L}")
            savedUserId.add(UserId(userId.id.value))
        }

        // when
        val users = repository.findByIds(savedUserId)

        // then
        assertTrue(users.isNotEmpty())
        assertEquals(userTotalCount, users.size)
        assertEquals(savedUserId, users.map { it.id })
    }

    @Test
    fun `findByDisplayId 성공 테스트`() = runTest {
        // given
        val savedUser = insertUser("1")
        val displayId = savedUser.displayId

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
        val savedUserEntity = insertUser("1")
        val userId = UserId(savedUserEntity.id.value)

        // when
        val result = repository.updateIntroduce(userId, TestIntroduce)
        val updatedUserEntity = repository.findVisibleById(userId, userId)

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

    private suspend fun insertUser(uniqueValue: String): User = TestDatabaseFactory.dbQuery {
        UserEntity
            .new {
                this.role = Role.USER
                this.provider = SocialLoginProvider.GOOGLE
                this.providerId = "pid$uniqueValue"
                this.displayId = "did$uniqueValue"
                this.name = "honggd"
                this.profileImageUrl = null
                this.introduce = "hello"
                this.isActive = true
                this.lastLoginAt = Instant.now()
            }.toDomain()
    }

    private suspend fun createBlock(
        blockerId: Long,
        blockedId: Long,
    ): BlockEntity = TestDatabaseFactory.dbQuery {
        BlockEntity.new {
            this.blockerId = EntityID(blockerId, Users)
            this.blockedId = EntityID(blockedId, Users)
            this.reasonId = EntityID(1, BlockReasons)
        }
    }

    companion object {
        private val TestUserPatch = UserPatch(
            userName = UserName("name"),
            profileImageUrl = null,
            introduce = Introduce("introduce"),
        )
        private val TestIntroduce = Introduce("introduce")
    }
}
