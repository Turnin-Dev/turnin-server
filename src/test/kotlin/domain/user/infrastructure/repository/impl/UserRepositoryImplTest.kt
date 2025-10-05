package com.peekr.domain.user.infrastructure.repository.impl

import com.peekr.common.db.schema.FriendStatus
import com.peekr.common.model.DisplayId
import com.peekr.common.model.Name
import com.peekr.common.model.UserId
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
    fun `findById 실패 테스트 - 사용자를 찾지 못하는 경우 null를 반환한다`() = runTest {
        // when
        val userEntity = repository.findById(UserId(1L))

        // then
        assertNull(userEntity)
    }

    @Test
    fun `findUserProfileById 성공 테스트`() = runTest {
        // given
        val savedUserEntity = TestDatabaseFactory.dbQuery {
            UserTestDoubles.saveAndGetUserEntity()
        }

        // when
        val userId = UserId(savedUserEntity.id.value)
        val userProfile = repository.findUserProfileById(userId)

        // then
        assertNotNull(userProfile)
        assertEquals(userId, userProfile.user.id)
    }

    @Test
    fun `findUserProfileById 성공 테스트 - 사용자의 친구(수락 된) 수를 정확히 반환한다`() = runTest {
        // given
        val user1Id = TestDatabaseFactory.dbQuery {
            val savedUserEntity1 = UserTestDoubles.saveAndGetUserEntity("g1", "user1")
            val savedEserEntity2 = UserTestDoubles.saveAndGetUserEntity("g2", "user2")
            val savedEserEntity3 = UserTestDoubles.saveAndGetUserEntity("g3", "user3")
            val savedEserEntity4 = UserTestDoubles.saveAndGetUserEntity("g4", "user4")
            UserTestDoubles.saveFriendEntity(
                requesterId = savedEserEntity2.id.value,
                receiverId = savedUserEntity1.id.value,
                status = FriendStatus.ACCEPTED,
            )
            UserTestDoubles.saveFriendEntity(
                requesterId = savedEserEntity3.id.value,
                receiverId = savedUserEntity1.id.value,
                status = FriendStatus.PENDING,
            )
            UserTestDoubles.saveFriendEntity(
                requesterId = savedEserEntity4.id.value,
                receiverId = savedUserEntity1.id.value,
                status = FriendStatus.REJECTED,
            )
            // 친구 신청 현황: 수락(1), 요청 중(1), 거절(1) -> 최종적으로 친구 관계는 1명

            UserId(savedUserEntity1.id.value)
        }

        // when
        val userProfile = repository.findUserProfileById(user1Id)

        // then
        assertNotNull(userProfile)
        assertEquals(1L, userProfile.friendsCount)
    }

    @Test
    fun `findUserProfileById 실패 테스트 - 사용자를 찾지 못하는 경우 null를 반환한다`() = runTest {
        // when
        val userProfile = repository.findUserProfileById(UserId(1L))

        // then
        assertNull(userProfile)
    }

    @Test
    fun `update 성공 테스트`() = runTest {
        // given
        val savedUserEntity = TestDatabaseFactory.dbQuery {
            UserTestDoubles.saveAndGetUserEntity()
        }

        // when
        val userId = UserId(savedUserEntity.id.value)
        val result = repository.update(userId, TestUserPatch)
        val updatedUserEntity = repository.findById(userId)

        // then
        assertTrue(result)
        assertEquals(TestUserPatch.displayId, updatedUserEntity?.displayId)
        assertEquals(TestUserPatch.name, updatedUserEntity?.name)
        assertEquals(TestUserPatch.profileImageUrl, updatedUserEntity?.profileImageUrl)
        assertEquals(TestUserPatch.introduce, updatedUserEntity?.introduce)
    }

    @Test
    fun `update 실패 테스트 - 사용자를 찾지 못하는 경우 false를 반환한다`() = runTest {
        // when
        val userId = UserId(1L)
        val result = repository.update(userId, TestUserPatch)

        // then
        assertFalse(result)
    }

    companion object {
        private val TestUserPatch = UserPatch(
            displayId = DisplayId("ididid"),
            name = Name("name"),
            profileImageUrl = null,
            introduce = "introduce",
        )
    }
}
