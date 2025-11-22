package com.peekr.domain.friend.application.usecase.e2e

import com.peekr.common.db.schema.UserEntity
import com.peekr.common.model.FriendStatus
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.id.UserId
import com.peekr.domain.friend.application.usecase.AddFriendUseCase
import com.peekr.domain.friend.application.usecase.DeleteFriendUseCase
import com.peekr.domain.friend.application.usecase.GetFriendsUseCase
import com.peekr.domain.friend.application.usecase.UpdateFriendStatusUseCase
import com.peekr.domain.friend.domain.provider.UserProvider
import com.peekr.domain.friend.domain.repository.FriendRepository
import com.peekr.domain.friend.infrastructure.provider.UserProviderImpl
import com.peekr.domain.friend.infrastructure.repository.FriendRepositoryImpl
import com.peekr.domain.user.application.provider.UserProviderApi
import com.peekr.domain.user.infrastructure.repository.impl.UserRepositoryImpl
import com.peekr.util.TestDatabaseFactory
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * 여러 유스케이스들을 활용한 시나리오 테스트 (통합 테스트)
 */
class FriendUseCaseIntegrationTest {
    // UserBC의 API
    private val userProvider: UserProvider = UserProviderImpl(UserProviderApi(UserRepositoryImpl()))
    private val friendRepository: FriendRepository = FriendRepositoryImpl()
    private val getFriendsUseCase = GetFriendsUseCase(friendRepository)
    private val addFriendUseCase = AddFriendUseCase(friendRepository, userProvider)
    private val updateFriendStatusUseCase = UpdateFriendStatusUseCase(friendRepository, userProvider)
    private val deleteFriendUseCase = DeleteFriendUseCase(friendRepository)

    @BeforeTest
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @AfterTest
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `(친구 요청, 수락 테스트) 사용자 A가 사용자 B에게 친구 요청을 하고 수락하면 사용자 B의 친구 목록 조회 시 사용자 A가 포함된다`() = runTest {
        // given: 사용자 A, B 생성
        val userA = insertUserAndReturnId("a")
        val userB = insertUserAndReturnId("b")

        // when
        // 1. 사용자 A가 사용자 B에게 친구 추가 요청
        val friendDto = addFriendUseCase(userA.value, userB.value)
        assertTrue(friendDto.requesterId == userA.value)
        assertTrue(friendDto.receiverId == userB.value)

        // 2. 사용자 B가 친구 요청 수락
        val result = updateFriendStatusUseCase(userB.value, userA.value, FriendStatus.ACCEPTED)
        assertTrue(result)

        // then: 사용자 B 친구 목록 조회
        val userBFriends = getFriendsUseCase(userB)
        assertTrue(userBFriends.isNotEmpty())
        assertTrue(
            userBFriends.first().requesterId == userA.value ||
                userBFriends.first().receiverId == userA.value,
        )
        assertTrue(
            userBFriends.first().requesterId == userB.value ||
                userBFriends.first().receiverId == userB.value,
        )
    }

    @Test
    fun `(친구 요청, 거절 테스트) 사용자 A가 사용자 B에게 친구 요청을 하고 사용자 B가 거절하면 사용자 A, B 둘 모두에게 친구 데이터가 삭제된다`() = runTest {
        // given: 사용자 A, B 생성
        val userA = insertUserAndReturnId("a")
        val userB = insertUserAndReturnId("b")

        // when
        // 1. 사용자 A가 사용자 B에게 친구 추가 요청
        val friendDto = addFriendUseCase(userA.value, userB.value)
        assertTrue(friendDto.requesterId == userA.value)
        assertTrue(friendDto.receiverId == userB.value)

        // 2. 사용자 B가 친구 요청 rjwjf
        val result = deleteFriendUseCase(userB.value, userA.value)
        assertTrue(result)

        // then: 사용자 A, 사용자 B 친구 목록 조회 시 전부 비어있어야 한다.
        val userBFriends = getFriendsUseCase(userB)
        val userAFriends = getFriendsUseCase(userA)
        assertTrue(userAFriends.isEmpty())
        assertTrue(userBFriends.isEmpty())
    }

    @Test
    fun `(친구 끊기(삭제) 테스트) 사용자 A와 사용자 B가 친구인 상태에서 사용자 A가 친구 관계를 끊으면 사용자 A, B 둘 모두에게 친구 데이터가 삭제된다`() = runTest {
        // given: 사용자 A, B 생성
        val userA = insertUserAndReturnId("a")
        val userB = insertUserAndReturnId("b")

        // when
        // 1. 사용자 A가 사용자 B에게 친구 추가 요청 후 수락
        val friendDto = addFriendUseCase(userA.value, userB.value)
        assertTrue(friendDto.requesterId == userA.value)
        assertTrue(friendDto.receiverId == userB.value)
        val updateResult = updateFriendStatusUseCase(userB.value, userA.value, FriendStatus.ACCEPTED)
        assertTrue(updateResult)

        // 2. 서로 친구 인지 확인
        val userAFriends = getFriendsUseCase(userA)
        val userBFriends = getFriendsUseCase(userB)
        assertTrue(userAFriends.isNotEmpty())
        assertTrue(userBFriends.isNotEmpty())

        // 3. 사용자 A가 사용자 B와 친구 관계 끊기(삭제)
        val deleteResult = deleteFriendUseCase(userA.value, userB.value)
        assertTrue(deleteResult)

        // then: 서로 친구 데이터가 삭제됐는지 확인
        val userAFriends2 = getFriendsUseCase(userA)
        val userBFriends2 = getFriendsUseCase(userB)
        assertTrue(userAFriends2.isEmpty())
        assertTrue(userBFriends2.isEmpty())
    }

    @Test
    fun `(친구 동시 취소, 거절 테스트) 사용자 A가 사용자 B에게 친구 요청을 하고 취소하는 동시에 친구 B가 거절하는 경우 친구 B는 실패 응답을 받는다`() = runTest {
        // given: 사용자 A, B 생성
        val userA = insertUserAndReturnId("a")
        val userB = insertUserAndReturnId("b")

        // when
        // 1. 사용자 A가 사용자 B에게 친구 추가 요청
        val friendDto = addFriendUseCase(userA.value, userB.value)
        assertTrue(friendDto.requesterId == userA.value)
        assertTrue(friendDto.receiverId == userB.value)

        // 2. 사용자 A가 친구 요청을 취소하고 사용자 B가 요청을 거절한다.
        val deleteResult1 = deleteFriendUseCase(userA.value, userB.value)
        val deleteResult2 = deleteFriendUseCase(userB.value, userA.value)

        // then: 사용자 B는 false를 반환 받는다
        assertTrue(deleteResult1)
        assertFalse(deleteResult2)
    }

    @Test
    fun `(친구 동시 취소, 수락 테스트) 사용자 A가 사용자 B에게 친구 요청을 하고 취소하는 동시에 친구 B가 수락하는 경우 실패 응답을 받는다`() = runTest {
        // given: 사용자 A, B 생성
        val userA = insertUserAndReturnId("a")
        val userB = insertUserAndReturnId("b")

        // when
        // 1. 사용자 A가 사용자 B에게 친구 추가 요청
        val friendDto = addFriendUseCase(userA.value, userB.value)
        assertTrue(friendDto.requesterId == userA.value)
        assertTrue(friendDto.receiverId == userB.value)

        // 2. 사용자 A가 친구 요청을 취소하고 사용자 B가 요청을 수락한다.
        val deleteResult1 = deleteFriendUseCase(userA.value, userB.value)
        val updateResult = updateFriendStatusUseCase(userB.value, userA.value, FriendStatus.ACCEPTED)

        // then: 사용자 B는 false를 반환 받는다
        assertTrue(deleteResult1)
        assertFalse(updateResult)
    }

    private suspend fun insertUserAndReturnId(uniqueValue: String): UserId = TestDatabaseFactory.dbQuery {
        val savedUser = UserEntity.Companion.new {
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

        UserId.Companion(savedUser.id.value)
    }
}
