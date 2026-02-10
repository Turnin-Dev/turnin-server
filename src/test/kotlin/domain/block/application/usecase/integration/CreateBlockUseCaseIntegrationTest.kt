package com.peekr.domain.block.application.usecase.integration

import com.peekr.common.db.schema.Blocks
import com.peekr.common.db.schema.FriendEntity
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.model.FriendRequestStatus
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.id.UserId
import com.peekr.domain.block.application.dto.BlockDetailDto
import com.peekr.domain.block.application.dto.toDto
import com.peekr.domain.block.application.usecase.CreateBlockUseCase
import com.peekr.domain.block.domain.model.Block
import com.peekr.domain.block.domain.provider.FriendProvider
import com.peekr.domain.block.domain.repository.BlockRepository
import com.peekr.domain.block.infrastructure.mapper.BlockMapper.toDomain
import com.peekr.domain.block.infrastructure.repository.BlockRepositoryImpl
import com.peekr.util.db.TestDatabaseFactory
import io.mockk.coEvery
import io.mockk.mockk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.junit.After
import org.junit.Before
import org.junit.Test

class CreateBlockUseCaseIntegrationTest {
    private val blockRepository = BlockRepositoryImpl()
    private val friendProvider = FakeFriendProvider()
    private lateinit var usecase: CreateBlockUseCase

    @Before
    fun setUp() {
        TestDatabaseFactory.init()

        usecase = CreateBlockUseCase(blockRepository, friendProvider)
    }

    @After
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `차단 생성 시 친구 삭제까지 정상적으로 수행된다`() = runTest {
        // given: 2명의 사용자 생성 후 친구 관계로 설정
        val user1 = insertUserAndReturn("1")
        val user2 = insertUserAndReturn("2")
        val friend = createFriends(user1.id, user2.id)
        // 친구 설정 잘 됐는지 확인
        assertNotNull(findFriendById(friend.id.value))

        // when: 차단 생성
        val blockDetailDto = BlockDetailDto(
            blockerId = user1.id.value,
            blockedId = user2.id.value,
            reasonId = 1L,
            customReason = "custom_reason",
        )
        usecase(blockDetailDto)

        // then: 차단 생성과 친구가 정상적으로 삭제됐는지 검증
        // 차단 생성 검증
        val blocks = getBlocks(blockerId = user1.id.value)
        assertEquals(1, blocks.size)
        assertEquals(blockDetailDto, blocks[0].detail.toDto())
        // 친구 삭제 검증
        assertNull(findFriendById(friend.id.value))
    }

    @Test
    fun `차단 생성에서 예외 발생 시 친구 삭제는 수행하지 않는다`() = runTest {
        // given: 2명의 사용자 생성 후 친구 관계로 설정
        val user1 = insertUserAndReturn("1")
        val user2 = insertUserAndReturn("2")
        val friend = createFriends(user1.id, user2.id)
        // 친구 설정 잘 됐는지 확인
        assertNotNull(findFriendById(friend.id.value))

        // 차단 생성 시 예외가 발생하도록 설정
        val mockBlockRepository = mockk<BlockRepository>()
        coEvery { mockBlockRepository.createBlock(any()) } throws Exception("error!")
        usecase = CreateBlockUseCase(mockBlockRepository, friendProvider)

        // when: 차단 생성
        val blockDetailDto = BlockDetailDto(
            blockerId = user1.id.value,
            blockedId = user2.id.value,
            reasonId = 1L,
            customReason = "custom_reason",
        )
        runCatching {
            usecase(blockDetailDto)
        }

        // then: 차단이 생성되지 않고 친구 관계는 그대로여야 한다.
        val blocks = getBlocks(blockerId = user1.id.value)
        assertTrue(blocks.isEmpty())
        assertNotNull(findFriendById(friend.id.value))
    }

    @Test
    fun `차단 생성 시 친구 삭제에서 예외 발생 시 롤백이 수행된다`() = runTest {
        // given: 2명의 사용자 생성 후 친구 관계로 설정
        val user1 = insertUserAndReturn("1")
        val user2 = insertUserAndReturn("2")
        val friend = createFriends(user1.id, user2.id)
        // 친구 설정 잘 됐는지 확인
        assertNotNull(findFriendById(friend.id.value))

        // 친구 삭제 시 에외가 발생하도록 설정
        val mockFriendProvider = mockk<FriendProvider>()
        coEvery {
            mockFriendProvider.deleteFriend(UserId(1L), UserId(2L))
        } throws Exception("error")
        usecase = CreateBlockUseCase(blockRepository, mockFriendProvider)

        // when: 차단 생성
        val blockDetailDto = BlockDetailDto(
            blockerId = user1.id.value,
            blockedId = user2.id.value,
            reasonId = 1L,
            customReason = "custom_reason",
        )
        runCatching {
            usecase(blockDetailDto)
        }

        // then: 차단이 생성되지 않고 친구 관계는 그대로여야 한다.
        val blocks = getBlocks(blockerId = user1.id.value)
        assertTrue(blocks.isEmpty())
        assertNotNull(findFriendById(friend.id.value))
    }

    private suspend fun insertUserAndReturn(
        uniqueValue: String,
    ): UserEntity = TestDatabaseFactory.dbQuery {
        UserEntity.new {
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

    private suspend fun createFriends(
        requesterId: EntityID<Long>,
        receiverId: EntityID<Long>,
        status: FriendRequestStatus = FriendRequestStatus.ACCEPTED,
    ): FriendEntity = TestDatabaseFactory.dbQuery {
        FriendEntity.new {
            this.requesterId = requesterId
            this.receiverId = receiverId
            this.status = status
        }
    }

    private suspend fun findFriendById(
        friendID: Long,
    ): FriendEntity? = TestDatabaseFactory.dbQuery {
        FriendEntity.findById(friendID)
    }

    private suspend fun getBlocks(
        blockerId: Long,
    ): List<Block> = TestDatabaseFactory.dbQuery {
        Blocks
            .selectAll()
            .where(Blocks.blockerId eq blockerId)
            .map { it.toDomain() }
    }
}
