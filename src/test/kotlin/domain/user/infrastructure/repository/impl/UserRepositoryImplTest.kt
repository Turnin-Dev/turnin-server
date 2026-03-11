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
import com.peekr.domain.user.domain.model.UserPatch
import com.peekr.domain.user.domain.repository.UserRepository
import com.peekr.domain.user.infrastructure.mapper.UserMapper.toDomain
import com.peekr.util.db.TestDatabaseFactory
import com.peekr.util.db.setUserInactiveForTest
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
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
        val userEntity = insertUser("1")
        val user = TestDatabaseFactory.dbQuery { userEntity.toDomain() }

        // when
        val userResult = repository.findById(user.id)

        // then
        assertNotNull(userResult)
        assertEquals(user, userResult)
    }

    @Test
    fun `findActiveById 성공 테스트`() = runTest {
        // given
        val userEntity = insertUser("1")
        val user = TestDatabaseFactory.dbQuery { userEntity.toDomain() }

        // when
        val userResult = repository.findActiveById(user.id)

        // then
        assertNotNull(userResult)
        assertEquals(user, userResult)
    }

    @Test
    fun `findActiveById 성공 테스트 - 비활성화 사용자는 조회되지 않는다`() = runTest {
        // given: 비활성화 사용자 생성
        val userEntity = insertUser("1")
        val user = TestDatabaseFactory.dbQuery { userEntity.toDomain() }
        setUserInactiveForTest(user.id)

        // when
        val userResult = repository.findActiveById(user.id)

        // then: 사용자가 조회되지 않는다.
        assertNull(userResult)
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
    fun `findVisibleById 성공 테스트 - 비활성화 사용자는 조회되지 않는다`() = runTest {
        // given: 비활성화 사용자 생성
        val currentUser = insertUser("current")
        val user = insertUser("1")
        val currentId = UserId(currentUser.id.value)
        val userId = UserId(user.id.value)
        setUserInactiveForTest(userId)

        // when
        val userResult = repository.findVisibleById(currentId, userId)

        // then: 사용자가 조회되지 않는다.
        assertNull(userResult)
    }

    @Test
    fun `findVisibleById 성공 테스트 - 내가 차단한 사용자를 조회 시 isBlocked가 true인 채로 조회된다`() = runTest {
        // given: 사용자 2명 생성 후 차단 관계 설정
        val meEntity = insertUser("1")
        val me = TestDatabaseFactory.dbQuery { meEntity.toDomain() }
        val userEntity = insertUser("2")
        val user = TestDatabaseFactory.dbQuery { userEntity.toDomain() }
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
        val meEntity = insertUser("1")
        val me = TestDatabaseFactory.dbQuery { meEntity.toDomain() }
        val userEntity = insertUser("2")
        val user = TestDatabaseFactory.dbQuery { userEntity.toDomain() }
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
        val meEntity = insertUser("1")
        val me = TestDatabaseFactory.dbQuery { meEntity.toDomain() }
        val userEntity = insertUser("2")
        val user = TestDatabaseFactory.dbQuery { userEntity.toDomain() }
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
    fun `findByIds 성공 테스트 - 비활성화 사용자는 조회되지 않는다`() = runTest {
        // given: 1명의 테스트 사용자를 생성, 사용자 비활성화
        val userEntity = insertUser("1")
        val user = TestDatabaseFactory.dbQuery { userEntity.toDomain() }
        setUserInactiveForTest(user.id)

        // when
        val users = repository.findByIds(listOf(user.id))

        // then: 사용자가 조회되지 않는다.
        assertEquals(0, users.size)
    }

    @Test
    fun `findByDisplayId 성공 테스트`() = runTest {
        // given
        val userEntity = insertUser("1")
        val savedUser = TestDatabaseFactory.dbQuery { userEntity.toDomain() }
        val displayId = savedUser.displayId

        // when
        val user = repository.findByDisplayId(displayId)

        // then
        assertNotNull(user)
        assertEquals(displayId, user.displayId)
    }

    @Test
    fun `findByDisplayId 성공 테스트 - 비활성화 사용자는 조회되지 않는다`() = runTest {
        // given: 비활성화 사용자 생성
        val userEntity = insertUser("1")
        val savedUser = TestDatabaseFactory.dbQuery { userEntity.toDomain() }
        val displayId = savedUser.displayId
        setUserInactiveForTest(savedUser.id)

        // when
        val user = repository.findByDisplayId(displayId)

        // then: 사용자가 조회되지 않는다.
        assertNull(user)
    }

    @Test
    fun `findByDisplayId 실패 테스트 - 사용자를 찾지 못하는 경우 null를 반환한다`() = runTest {
        // when
        val userEntity = repository.findByDisplayId(DisplayId("did"))

        // then
        assertNull(userEntity)
    }

    @Test
    fun `update 성공 테스트`() = runTest {
        // given
        val user = insertUser("1")
        val originalUpdatedAt = TestDatabaseFactory.dbQuery { user.updatedAt }
        val userPatch = UserPatch(
            userName = UserName("newName"),
            displayId = DisplayId("newDid"),
            oldProfileImageUrl = "oldImageUrl",
            newProfileImageUrl = "newImageUrl",
            introduce = Introduce("newIntroduce"),
        )

        // when
        val result = repository.update(UserId(user.id.value), userPatch)
        val updatedUser = TestDatabaseFactory.dbQuery { UserEntity.findById(user.id.value) }
        val updatedAt = TestDatabaseFactory.dbQuery { updatedUser?.updatedAt }

        // then
        assertTrue(result)
        assertNotNull(updatedUser)
        assertEquals(userPatch.userName.value, updatedUser.name)
        assertEquals(userPatch.displayId.value, updatedUser.displayId)
        assertEquals(userPatch.introduce.value, updatedUser.introduce)
        assertEquals(userPatch.newProfileImageUrl, updatedUser.profileImageUrl)
        assertNotEquals(userPatch.oldProfileImageUrl, updatedUser.profileImageUrl)
        assertTrue(updatedAt!!.isAfter(originalUpdatedAt))
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
        val originalUpdatedAt = TestDatabaseFactory.dbQuery { savedUserEntity.updatedAt }
        val userId = UserId(savedUserEntity.id.value)

        // when
        val result = repository.updateIntroduce(userId, TestIntroduce)
        val updatedUserEntity = TestDatabaseFactory.dbQuery { UserEntity.findById(savedUserEntity.id.value) }
        val updatedAt = TestDatabaseFactory.dbQuery { updatedUserEntity?.updatedAt }

        // then
        assertTrue(result)
        assertEquals(TestIntroduce.value, updatedUserEntity?.introduce)
        assertTrue(updatedAt!!.isAfter(originalUpdatedAt))
    }

    @Test
    fun `updateIntroduce 실패 테스트 - 사용자를 찾지 못하는 경우 false를 반환한다`() = runTest {
        // when
        val userId = UserId(1L)
        val result = repository.updateIntroduce(userId, TestIntroduce)

        // then
        assertFalse(result)
    }

    @Test
    fun `deactivate 성공 테스트`() = runTest {
        // given: 사용자 생성
        val userEntity = insertUser("1")
        val user = TestDatabaseFactory.dbQuery { userEntity.toDomain() }
        assertNotNull(findByIdForTest(user.id.value))

        // when: 사용자 비활성화
        repository.deactivate(user.id)

        // then: 비활성화 됐는지 검증
        val foundedUser = findByIdForTest(user.id.value)
        assertNotNull(foundedUser)
        assertFalse(foundedUser.isActive)
        assertEquals("", foundedUser.introduce)
        assertEquals("탈퇴한 사용자", foundedUser.name)
        assertNull(foundedUser.profileImageUrl)
    }

    @Test
    fun `deactivate 성공 테스트 - 비활성화 시 findById, findVisibleById를 수행하는 경우 조회되지 않는다`() = runTest {
        // given: 사용자 생성
        val userEntity = insertUser("1")
        val user = TestDatabaseFactory.dbQuery { userEntity.toDomain() }
        val originalUpdatedAt = TestDatabaseFactory.dbQuery { userEntity.updatedAt }
        assertNotNull(findByIdForTest(user.id.value))

        // when: 사용자 비활성화 후 findById, findVisibleById로 조회
        repository.deactivate(user.id)
        val foundedUser1 = repository.findActiveById(user.id)
        val foundedUser2 = repository.findVisibleById(user.id, user.id)
        val updatedAt = TestDatabaseFactory.dbQuery {
            UserEntity.findById(user.id.value)?.updatedAt
        }

        // then: 비활성화 됐는지 검증
        assertNull(foundedUser1)
        assertNull(foundedUser2)
        // 업데이트 시간 검증
        assertTrue(updatedAt!!.isAfter(originalUpdatedAt))
    }

    @Test
    fun `anonymizeProviderId 성공 테스트`() = runTest {
        // given: 사용자 생성
        val userEntity = insertUser("1")
        val user = TestDatabaseFactory.dbQuery { userEntity.toDomain() }
        val originalUpdatedAt = TestDatabaseFactory.dbQuery { userEntity.updatedAt }
        val originalProviderId = user.providerId

        // when: providerId 비식별화
        val result = repository.anonymizeProviderId(user.id, user.providerId)
        val updatedAt = TestDatabaseFactory.dbQuery {
            UserEntity.findById(user.id.value)?.updatedAt
        }

        // then: 변조됐는지 검증
        assertTrue(result)
        val foundedUser = findByIdForTest(user.id.value)
        assertNotNull(foundedUser)
        assertTrue(foundedUser.providerId.startsWith("DELETED_"))
        assertTrue(foundedUser.providerId.endsWith("_$originalProviderId"))
        assertNotEquals(originalProviderId, foundedUser.providerId)
        assertTrue(updatedAt!!.isAfter(originalUpdatedAt))
    }

    private suspend fun insertUser(uniqueValue: String): UserEntity = TestDatabaseFactory.dbQuery {
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
            }
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

    private suspend fun findByIdForTest(userId: Long): UserEntity? = TestDatabaseFactory.dbQuery {
        UserEntity.findById(userId)
    }

    companion object {
        private val TestUserPatch = UserPatch(
            userName = UserName("name"),
            displayId = DisplayId("did"),
            oldProfileImageUrl = null,
            newProfileImageUrl = null,
            introduce = Introduce("introduce"),
        )
        private val TestIntroduce = Introduce("introduce")
    }
}
