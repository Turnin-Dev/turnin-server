package com.peekr.domain.block.infrastructure.repository

import com.peekr.common.db.schema.Blocks
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.db.schema.Users
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.id.UserId
import com.peekr.domain.block.domain.model.BlockDetail
import com.peekr.domain.block.infrastructure.mapper.BlockMapper.toDomain
import com.peekr.util.db.TestDatabaseFactory
import java.time.Instant
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.selectAll
import org.junit.After
import org.junit.Before
import org.junit.Test

class BlockRepositoryImplTest {
    private val repository = BlockRepositoryImpl()

    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @After
    fun teardown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `차단 생성 성공 테스트`() = runTest {
        // given: 2명의 사용자 생성
        val user1 = insertUserAndReturnId("1")
        val user2 = insertUserAndReturnId("2")
        val blockReasons = repository.getBlockReasons()
        val blockDetail = BlockDetail(
            blockerId = user1,
            blockedId = user2,
            reasonId = blockReasons.first().id,
            customReason = "custom-reason",
        )

        // when: 차단 생성
        repository.createBlock(blockDetail)

        // then: 차단 테이블의 행이 있나 조회
        val result = TestDatabaseFactory.dbQuery {
            Blocks
                .selectAll()
                .where(Blocks.blockerId eq user1.value)
                .map { it.toDomain() }
        }

        assertEquals(1, result.size)
        assertEquals(blockDetail, result.first().detail)
    }

    @Test
    fun `차단 목록 조회 페이지네이션 성공 테스트`() = runTest {
        // given: 차단하는 사용자 1명, 차단당하는 사용자 15명 생성
        val blocker = insertUserAndReturnId("blocker")
        val blockedUsers = batchInsertUserAndReturnIds(startId = 1, endId = 15)

        val blockReasons = repository.getBlockReasons()
        val firstReasonId = blockReasons.first().id

        // 15개의 차단 데이터 생성
        blockedUsers.forEach { blockedUser ->
            repository.createBlock(
                BlockDetail(
                    blockerId = blocker,
                    blockedId = blockedUser,
                    reasonId = firstReasonId,
                    customReason = "test reason",
                ),
            )
        }

        // when: 첫 번째 페이지 조회 (offset=0, size=10)
        val firstPage = repository.getBlocksById(
            userId = blocker,
            offset = 0,
            size = 10,
        )

        // then: 첫 페이지 검증
        assertEquals(15, firstPage.totalSize) // 전체 차단 수
        assertEquals(10, firstPage.blocks.size) // 현재 페이지 항목 수
        assertEquals(
            blocker,
            firstPage.blocks
                .first()
                .detail.blockerId,
        ) // 차단자 확인

        // when: 두 번째 페이지 조회 (offset=10, size=10)
        val secondPage = repository.getBlocksById(
            userId = blocker,
            offset = 10,
            size = 10,
        )

        // then: 두 번째 페이지 검증
        assertEquals(15, secondPage.totalSize) // 전체 차단 수 동일
        assertEquals(5, secondPage.blocks.size) // 나머지 5개만 조회

        // when: 세 번째 페이지 조회 (offset=20, size=10) - 데이터 없음
        val thirdPage = repository.getBlocksById(
            userId = blocker,
            offset = 20,
            size = 10,
        )

        // then: 빈 페이지 검증
        assertEquals(15, thirdPage.totalSize) // 전체 개수는 동일
        assertEquals(0, thirdPage.blocks.size) // 조회된 항목 없음

        // 정렬 확인: ID 내림차순 (최신 차단이 먼저)
        val allBlocks = firstPage.blocks + secondPage.blocks
        val sortedIds = allBlocks.map { it.id.value }
        assertEquals(sortedIds, sortedIds.sortedDescending())

        // 중복 확인: 모든 차단 ID가 고유한지
        val uniqueBlockIds = allBlocks.map { it.id.value }.toSet()
        assertEquals(15, uniqueBlockIds.size)
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

    private suspend fun batchInsertUserAndReturnIds(startId: Int, endId: Int): List<UserId> =
        TestDatabaseFactory.dbQuery {
            Users
                .batchInsert((startId..endId).toList()) { index ->
                    this[Users.role] = Role.USER
                    this[Users.provider] = SocialLoginProvider.GOOGLE
                    this[Users.providerId] = "pid_blocked$index"
                    this[Users.displayId] = "did_blocked$index"
                    this[Users.name] = "blocked_user$index"
                    this[Users.profileImageUrl] = null
                    this[Users.introduce] = "hello"
                    this[Users.isActive] = true
                    this[Users.lastLoginAt] = Instant.now()
                }.map { UserId(it[Users.id].value) }
        }
}
