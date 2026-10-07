package com.turnin.domain.userKeyword.infrastructure.repository.impl

import com.turnin.common.db.DatabaseException
import com.turnin.common.db.schema.BlockEntity
import com.turnin.common.db.schema.BlockReasons
import com.turnin.common.db.schema.KeywordEntity
import com.turnin.common.db.schema.Keywords
import com.turnin.common.db.schema.ReportReasons
import com.turnin.common.db.schema.Reports
import com.turnin.common.db.schema.UserEntity
import com.turnin.common.db.schema.UserKeywordEntity
import com.turnin.common.db.schema.UserKeywords
import com.turnin.common.db.schema.Users
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.userKeyword.domain.model.Description
import com.turnin.domain.userKeyword.domain.model.UserKeywordPatch
import com.turnin.util.db.TestDatabaseFactory
import com.turnin.util.db.setUserInactiveForTest
import com.turnin.util.db.setUserKeywordInactiveForTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.junit.After
import org.junit.Before
import org.junit.jupiter.api.assertThrows

class UserKeywordRepositoryImplTest {
    private val repository = UserKeywordRepositoryImpl()

    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @After
    fun teardown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `findById 성공 테스트`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )

        // when
        val actualUserKeyword = repository.findById(userKeyword.id)

        // then
        assertNotNull(actualUserKeyword)
        assertEquals(userKeyword, actualUserKeyword)
    }

    @Test
    fun `findById 성공 테스트 - 비활성화 사용자 키워드는 조회되지 않는다`() = runTest {
        // given: 비활성화 사용자 키워드 생성
        val userId = insertUserAndReturnId("1")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )
        setUserKeywordInactiveForTest(userKeyword.id)

        // when
        val actualUserKeyword = repository.findById(userKeyword.id)

        // then: 사용자 키워드가 조회되지 않는다.
        assertNull(actualUserKeyword)
    }

    @Test
    fun `findById 실패 테스트 - 데이터가 없으면 null을 반환한다`() = runTest {
        // when
        val actualUserKeyword = repository.findById(UserKeywordId(1L))

        // then
        assertNull(actualUserKeyword)
    }

    @Test
    fun `findListByUserId 성공 테스트`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )

        // when
        val userKeywords = repository.findListByUserId(userId)

        // then
        assertEquals(1, userKeywords.size)
        assertEquals(userKeywords.first().id, userKeyword.id)
        assertEquals(userKeywords.first().keywordId, keywordId)
    }

    @Test
    fun `findListByUserId 성공 테스트 - 비활성화 사용자 키워드는 조회되지 않는다`() = runTest {
        // given: 비활성화 사용자 키워드 생성
        val userId = insertUserAndReturnId("1")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )
        setUserKeywordInactiveForTest(userKeyword.id)

        // when
        val userKeywords = repository.findListByUserId(userId)

        // then: 조회되지 않는다.
        assertEquals(0, userKeywords.size)
    }

    @Test
    fun `findListByUserId 성공 테스트 - 등록된 키워드가 없는 상태에서 조회 시 빈 리스트를 반환한다`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")

        // when
        val userKeywords = repository.findListByUserId(userId)

        // then
        assertTrue(userKeywords.isEmpty())
    }

    @Test
    fun `findListByUserId 실패 테스트 - 존재하지 않는 사용자의 사용자 키워드 조회 시 빈 리스트를 반환한다`() = runTest {
        val userKeywords = repository.findListByUserId(UserId(10))

        assertTrue(userKeywords.isEmpty())
    }

    @Test
    fun `create 성공 테스트`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)

        // when
        val savedUserKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )

        // then
        assertEquals(savedUserKeyword.keywordId, keywordId)
        assertEquals(savedUserKeyword.userId, userId)
    }

    @Test
    fun `create 실패 테스트 - 외래키 제약 위반 발생 시 알려진 예외가 발생한다`() = runTest {
        assertThrows<DatabaseException.ForeignKeyViolationException> {
            repository.create(
                keywordId = KeywordId(1),
                userId = UserId(1),
                description = TestDescription,
            )
        }
    }

    @Test
    fun `findByKeywordIdAndUserId 성공 테스트`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )

        // when
        val actualUserKeyword = repository.findByKeywordIdAndUserId(keywordId, userId)

        // then
        assertNotNull(actualUserKeyword)
        assertEquals(userKeyword, actualUserKeyword)
    }

    @Test
    fun `findByKeywordIdAndUserId 성공 테스트 - 비활성화 사용자 키워드는 조회되지 않는다`() = runTest {
        // given: 비활성화 사용자 키워드 생성
        val userId = insertUserAndReturnId("1")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )
        setUserKeywordInactiveForTest(userKeyword.id)

        // when
        val actualUserKeyword = repository.findByKeywordIdAndUserId(keywordId, userId)

        // then: 조회되지 않는다.
        assertNull(actualUserKeyword)
    }

    @Test
    fun `findByKeywordIdAndUserId 실패 테스트 - 존재하지 않는 사용자의 사용자 키워드 조회 시 null을 반환한다`() = runTest {
        val actualUserKeyword = repository.findByKeywordIdAndUserId(KeywordId(10), UserId(10))

        assertNull(actualUserKeyword)
    }

    @Test
    fun `update 성공 테스트`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )
        val originalUserKeyword = TestDatabaseFactory.dbQuery {
            UserKeywordEntity.findById(userKeyword.id.value)?.updatedAt
        }

        val newKeywordId = insertKeywordAndReturnId(userId, "newKeyword")
        val newDescription = Description("newDescription")
        val patch = UserKeywordPatch(
            userKeywordId = userKeyword.id,
            keywordId = newKeywordId,
            description = newDescription,
        )

        // when
        val result = repository.update(userId, patch)
        val patchedUserKeyword = repository.findById(userKeyword.id)
        val updatedAt = TestDatabaseFactory.dbQuery {
            UserKeywordEntity.findById(patchedUserKeyword!!.id.value)?.updatedAt
        }

        // then
        assertTrue(result)
        assertNotNull(patchedUserKeyword)
        assertEquals(newKeywordId, patchedUserKeyword.keywordId)
        assertEquals(newDescription, patchedUserKeyword.description)
        assertTrue(updatedAt!!.isAfter(originalUserKeyword))
    }

    @Test
    fun `update 실패 테스트 - 존재하지 않는 사용자 ID 혹은 사용자 키워드 ID 조회 시 false 반환`() = runTest {
        val patch = UserKeywordPatch(
            userKeywordId = UserKeywordId(10),
            keywordId = KeywordId(101L),
            description = Description("newDescription"),
        )
        val result = repository.update(UserId(10), patch)

        assertFalse(result)
    }

    @Test
    fun `delete 성공 테스트`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )

        // when
        val result = repository.delete(userId, userKeyword.id)

        // then
        assertTrue(result)
    }

    @Test
    fun `delete 실패 테스트 - 존재하지 않는 사용자 ID 혹은 사용자 키워드 ID 조회 시 false 반환`() = runTest {
        val result = repository.delete(UserId(10), UserKeywordId(10))

        assertFalse(result)
    }

    @Test
    fun `deleteByUserId 성공 테스트`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )

        // when
        repository.deleteByUserId(userId)

        // then
        val count = repository.countByUserId(userId)
        assertEquals(0, count)
    }

    @Test
    fun `deleteByUserId 성공 테스트 - 사용자 키워드가 존재하지 않는 경우 정상 종료`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")

        // when
        repository.deleteByUserId(userId)

        // then
        val count = repository.countByUserId(userId)
        assertEquals(0, count)
    }

    @Test
    fun `deleteUnreportedByUserId 성공 테스트 - 신고 내역이 없는 사용자 키워드는 삭제된다`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = createForTest(userId = userId, keywordId = keywordId, description = TestDescription)

        // when
        repository.deleteUnreportedByUserId(userId)

        // then
        assertNull(findByIdForTest(userKeyword.id.value))
    }

    @Test
    fun `deleteUnreportedByUserId 성공 테스트 - 신고 내역이 있는 사용자 키워드는 유지된다`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        val reporterId = insertUserAndReturnId("2")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = createForTest(userId = userId, keywordId = keywordId, description = TestDescription)
        insertUserKeywordReport(reporterId = reporterId, userKeywordId = userKeyword.id.value)

        // when
        repository.deleteUnreportedByUserId(userId)

        // then
        assertNotNull(findByIdForTest(userKeyword.id.value))
    }

    @Test
    fun `deleteUnreportedByUserId 성공 테스트 - 다른 사용자의 사용자 키워드는 유지된다`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        val otherUserId = insertUserAndReturnId("2")
        val keywordId = insertKeywordAndReturnId(otherUserId, TEST_KEYWORD)
        val otherUserKeyword = createForTest(
            userId = otherUserId,
            keywordId = keywordId,
            description = TestDescription,
        )

        // when
        repository.deleteUnreportedByUserId(userId)

        // then
        assertNotNull(findByIdForTest(otherUserKeyword.id.value))
    }

    @Test
    fun `findDescriptionById 성공 테스트`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )

        // when
        val description = repository.findDescriptionById(userId, userKeyword.id)

        // then
        assertNotNull(description)
        assertEquals(description, TestDescription)
    }

    @Test
    fun `findDescriptionById 성공 테스트 - 비활성화 사용자 키워드는 조회되지 않는다`() = runTest {
        // given: 비활성화 사용자 키워드 생성
        val userId = insertUserAndReturnId("1")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )
        setUserKeywordInactiveForTest(userKeyword.id)

        // when
        val description = repository.findDescriptionById(userId, userKeyword.id)

        // then: 조회되지 않는다.
        assertNull(description)
    }

    @Test
    fun `findDescriptionById 성공 테스트 - 등록되지 않은 사용자 키워드 조회 시 null을 반환한다`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")

        // when
        val description = repository.findDescriptionById(userId, UserKeywordId(1L))

        // then
        assertNull(description)
    }

    @Test
    fun `findDescriptionById 성공 테스트 - 사용자 키워드 설명이 비어있는 경우 null을 반환한다`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = Description(null),
        )

        // when
        val description = repository.findDescriptionById(userId, userKeyword.id)

        // then
        assertNull(description)
    }

    @Test
    fun `getDetailById 성공 테스트 - 사용자 정보 포함`() = runTest {
        // given
        val currentUserId = insertUserAndReturnId("1")
        val userId = insertUserAndReturnId("2")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )

        // when
        val userKeywordDetail = repository.getDetailById(currentUserId, userKeyword.id)

        // then
        assertEquals(TestDescription.value, userKeywordDetail?.description?.value)
        assertEquals(userId, userKeywordDetail?.userInfo?.userId)
        assertEquals(keywordId, userKeywordDetail?.keywordId)
    }

    @Test
    fun `getDetailById 성공 테스트 - 비활성화 사용자는 조회되지 않는다`() = runTest {
        // given: 비활성화 사용자 생성
        val currentUserId = insertUserAndReturnId("1")
        val userId = insertUserAndReturnId("2")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )
        setUserInactiveForTest(userId)

        // when
        val userKeywordDetail = repository.getDetailById(currentUserId, userKeyword.id)

        // then: 키워드 상세정보가 조회되지 않는다.
        assertNull(userKeywordDetail)
    }

    @Test
    fun `getDetailById 성공 테스트 - 비활성화 사용자 키워드는 조회되지 않는다`() = runTest {
        // given: 비활성화 사용자 키워드 생성
        val currentUserId = insertUserAndReturnId("1")
        val userId = insertUserAndReturnId("2")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )
        setUserKeywordInactiveForTest(userKeyword.id)

        // when
        val userKeywordDetail = repository.getDetailById(currentUserId, userKeyword.id)

        // then: 키워드 상세정보가 조회되지 않는다.
        assertNull(userKeywordDetail)
    }

    @Test
    fun `getDetailById 성공 테스트 - 차단된 사용자끼리는 서로 조회되지 않는다(null 반환)`() = runTest {
        // given: 사용자 2명을 생성하여 차단 관계를 만든다, 각 사용자의 키워드를 생성한다.
        val user1 = insertUserAndReturnId("1")
        val user2 = insertUserAndReturnId("2")
        createBlock(user1.value, user2.value)
        // user1 기준으로 사용자 키워드를 생성
        val keywordId1 = insertKeywordAndReturnId(user1, "user1keyword")
        val userKeyword1 = repository.create(keywordId1, user1, TestDescription)
        // user2 기준으로 사용자 키워드를 생성
        val keywordId2 = insertKeywordAndReturnId(user2, "user2keyword")
        val userKeyword2 = repository.create(keywordId2, user2, TestDescription)

        // when: user1과 user2 서로 키워드 상세 정보를 조회
        val userKeywordDetail = repository.getDetailById(user1, userKeyword2.id)
        val userKeywordDetail2 = repository.getDetailById(user2, userKeyword1.id)

        // then: user1과 user2는 차단 관계이므로 조회되지 않는다.
        assertNull(userKeywordDetail)
        assertNull(userKeywordDetail2)
    }

    @Test
    fun `getDetailsByUserId 성공 테스트`() = runTest {
        // given
        val currentUserId = insertUserAndReturnId("1")
        val userId = insertUserAndReturnId("2")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )

        // when
        val userKeywordDetails = repository.getDetailsByUserId(currentUserId, userId)

        // then
        assertEquals(1, userKeywordDetails.size)
        assertEquals(userKeyword.id, userKeywordDetails.first().userKeywordId)
        assertEquals(userKeyword.keywordId, userKeywordDetails.first().keywordId)
    }

    @Test
    fun `getDetailsByUserId 성공 테스트 - 비활성화 사용자는 조회되지 않는다`() = runTest {
        // given: 비활성화 사용자 생성
        val currentUserId = insertUserAndReturnId("1")
        val userId = insertUserAndReturnId("2")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )
        setUserInactiveForTest(userId)

        // when
        val userKeywordDetails = repository.getDetailsByUserId(currentUserId, userId)

        // then: 키워드 상세정보가 조회되지 않는다.
        assertEquals(0, userKeywordDetails.size)
    }

    @Test
    fun `getDetailsByUserId 성공 테스트 - 비활성화 사용자 키워드는 조회되지 않는다`() = runTest {
        // given: 비활성화 사용자 키워드 생성
        val currentUserId = insertUserAndReturnId("1")
        val userId = insertUserAndReturnId("2")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )
        setUserKeywordInactiveForTest(userKeyword.id)

        // when
        val userKeywordDetails = repository.getDetailsByUserId(currentUserId, userId)

        // then: 키워드 상세정보가 조회되지 않는다.
        assertEquals(0, userKeywordDetails.size)
    }

    @Test
    fun `getDetailsByUserId 성공 테스트 - 차단된 사용자끼리는 서로 조회되지 않는다(빈 리스트 반환)`() = runTest {
        // given: 사용자 2명을 생성하여 차단 관계를 만든다, 각 사용자의 키워드를 생성한다.
        val user1 = insertUserAndReturnId("1")
        val user2 = insertUserAndReturnId("2")
        createBlock(user1.value, user2.value)
        // user1 기준으로 사용자 키워드를 생성
        val keywordId1 = insertKeywordAndReturnId(user1, "user1keyword")
        repository.create(keywordId1, user1, TestDescription)
        // user2 기준으로 사용자 키워드를 생성
        val keywordId2 = insertKeywordAndReturnId(user2, "user2keyword")
        repository.create(keywordId2, user2, TestDescription)

        // when: user1과 user2 서로 키워드 상세 정보를 조회
        val userKeywordDetail = repository.getDetailsByUserId(user1, user2)
        val userKeywordDetail2 = repository.getDetailsByUserId(user2, user1)

        // then: user1과 user2는 차단 관계이므로 조회되지 않는다.
        assertTrue(userKeywordDetail.isEmpty())
        assertTrue(userKeywordDetail2.isEmpty())
    }

    @Test
    fun `getDetailsByUserId 성공 테스트 - 데이터가 없는 경우 빈 리스트를 반환한다`() = runTest {
        // when
        val userKeywordDetails = repository.getDetailsByUserId(UserId(1L), UserId(100L))

        // then
        assertTrue(userKeywordDetails.isEmpty())
    }

    @Test
    fun `countByUserId 성공 테스트`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )

        // when
        val count = repository.countByUserId(userId)

        // then
        assertEquals(1, count)
    }

    @Test
    fun `countByUserId 성공 테스트 - 비활성화 사용자 키워드는 제외된다`() = runTest {
        // given: 비활성화 사용자 키워드 생성
        val userId = insertUserAndReturnId("1")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )
        setUserKeywordInactiveForTest(userKeyword.id)

        // when
        val count = repository.countByUserId(userId)

        // then
        assertEquals(0, count)
    }

    @Test
    fun `deactivate 성공 테스트`() = runTest {
        // given: 사용자 키워드 생성
        val userId = insertUserAndReturnId("1")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )
        val originalUpdatedAt = TestDatabaseFactory.dbQuery {
            UserKeywordEntity.findById(userKeyword.id.value)?.updatedAt
        }

        // when: 비활성화
        val result = repository.deactivate(userId, userKeyword.id)
        val foundedUserKeyword = TestDatabaseFactory.dbQuery {
            UserKeywordEntity.findById(userKeyword.id.value)
        }
        val updatedAt = TestDatabaseFactory.dbQuery {
            UserKeywordEntity.findById(foundedUserKeyword!!.id.value)?.updatedAt
        }

        // then: 검증
        assertTrue(result)
        assertNotNull(foundedUserKeyword)
        assertFalse(foundedUserKeyword.isActive)
        assertTrue(updatedAt!!.isAfter(originalUpdatedAt))
    }

    @Test
    fun `deactivate 성공 테스트 - 비활성화후 findById로 조회되지 않는다`() = runTest {
        // given: 사용자 키워드 생성
        val userId = insertUserAndReturnId("1")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )

        // when: 비활성화 후 findById 조회
        val result = repository.deactivate(userId, userKeyword.id)
        val foundedUserKeyword = repository.findById(userKeyword.id)

        // then: 조회되지 않는다.
        assertTrue(result)
        assertNull(foundedUserKeyword)
    }

    @Test
    fun `softDeleteAll 성공 테스트 - 모든 키워드가 비활성화된다`() = runTest {
        // given: 사용자 키워드 생성
        val userId = insertUserAndReturnId("1")
        val keywordId1 = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val keywordId2 = insertKeywordAndReturnId(userId, "keyword2")
        val userKeyword1 = createForTest(
            keywordId = keywordId1,
            userId = userId,
            description = TestDescription,
        )
        val userKeyword2 = createForTest(
            keywordId = keywordId2,
            userId = userId,
            description = Description("desc 2"),
        )
        val originalUpdatedAtList = listOf(
            TestDatabaseFactory.dbQuery {
                UserKeywordEntity.findById(userKeyword1.id.value)?.updatedAt
            },
            TestDatabaseFactory.dbQuery {
                UserKeywordEntity.findById(userKeyword2.id.value)?.updatedAt
            },
        )
        assertNotNull(findByIdForTest(userKeyword1.id.value))
        assertNotNull(findByIdForTest(userKeyword2.id.value))

        // when: 모두 Soft Delete
        repository.softDeleteAll(userId)

        // then: 모두 비활성화 됐는지 검증
        val userKeywordActiveList = TestDatabaseFactory.dbQuery {
            UserKeywords
                .selectAll()
                .where { UserKeywords.userId eq userId.value }
                .map { it[UserKeywords.isActive] }
        }

        val updatedAtList = TestDatabaseFactory.dbQuery {
            UserKeywordEntity.find { UserKeywords.userId eq userId.value }.map { it.updatedAt }
        }

        assertTrue(userKeywordActiveList.isNotEmpty())
        assertTrue(userKeywordActiveList.all { !it })
        updatedAtList.zip(originalUpdatedAtList) { updatedAt, originalUpdatedAt ->
            assertTrue(updatedAt.isAfter(originalUpdatedAt))
        }
    }

    @Test
    fun `softDeleteAll 성공 테스트 - 삭제 시각이 기록된다`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = createForTest(userId = userId, keywordId = keywordId, description = TestDescription)

        // when
        repository.softDeleteAll(userId)

        // then
        assertNotNull(findDeletedAtForTest(userKeyword.id.value))
    }

    @Test
    fun `softDeleteAll 성공 테스트 - 이미 삭제 시각이 기록된 키워드는 기존 삭제 시각을 유지한다`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = createForTest(userId = userId, keywordId = keywordId, description = TestDescription)
        setSoftDeletedForTest(userKeyword.id.value, Instant.parse("2026-01-01T00:00:00Z"))

        // when
        repository.softDeleteAll(userId)

        // then
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), findDeletedAtForTest(userKeyword.id.value))
    }

    @Test
    fun `softDelete 성공 테스트 - 본인 키워드가 비활성화되고 삭제 시각이 기록되며 true를 반환한다`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = createForTest(userId = userId, keywordId = keywordId, description = TestDescription)

        // when
        val result = repository.softDelete(userId, UserKeywordId(userKeyword.id.value))

        // then
        assertTrue(result)
        val found = findByIdForTest(userKeyword.id.value)
        assertFalse(found!!.isActive)
        assertNotNull(found.deletedAt)
    }

    @Test
    fun `softDelete 실패 테스트 - 다른 사용자의 키워드는 변경하지 않고 false를 반환한다`() = runTest {
        // given
        val ownerId = insertUserAndReturnId("1")
        val otherUserId = insertUserAndReturnId("2")
        val keywordId = insertKeywordAndReturnId(ownerId, TEST_KEYWORD)
        val userKeyword = createForTest(userId = ownerId, keywordId = keywordId, description = TestDescription)

        // when
        val result = repository.softDelete(otherUserId, UserKeywordId(userKeyword.id.value))

        // then
        assertFalse(result)
        val found = findByIdForTest(userKeyword.id.value)
        assertTrue(found!!.isActive)
        assertNull(found.deletedAt)
    }

    @Test
    fun `findIdsDeletedBefore 성공 테스트 - 기준 시각 이전에 삭제된 키워드만 반환한다`() = runTest {
        // given: 기준 이전 삭제, 기준 이후 삭제, 삭제되지 않은 키워드
        val userId = insertUserAndReturnId("1")
        val expired = createForTest(userId, insertKeywordAndReturnId(userId, "keyword1"), TestDescription)
        val notExpired = createForTest(userId, insertKeywordAndReturnId(userId, "keyword2"), TestDescription)
        createForTest(userId, insertKeywordAndReturnId(userId, "keyword3"), TestDescription)
        setSoftDeletedForTest(expired.id.value, Instant.parse("2025-01-01T00:00:00Z"))
        setSoftDeletedForTest(notExpired.id.value, Instant.parse("2025-12-01T00:00:00Z"))

        // when
        val result = repository.findIdsDeletedBefore(
            deletedBefore = Instant.parse("2025-06-01T00:00:00Z"),
            limit = 100,
            afterId = null,
        )

        // then
        assertEquals(listOf(expired.id.value), result)
    }

    @Test
    fun `findIdsDeletedBefore 성공 테스트 - afterId보다 큰 ID부터 limit 개수만큼 반환한다`() = runTest {
        // given: 기준 이전에 삭제된 키워드 3개
        val userId = insertUserAndReturnId("1")
        val first = createForTest(userId, insertKeywordAndReturnId(userId, "keyword1"), TestDescription)
        val second = createForTest(userId, insertKeywordAndReturnId(userId, "keyword2"), TestDescription)
        val third = createForTest(userId, insertKeywordAndReturnId(userId, "keyword3"), TestDescription)
        listOf(first, second, third).forEach {
            setSoftDeletedForTest(it.id.value, Instant.parse("2025-01-01T00:00:00Z"))
        }

        // when
        val result = repository.findIdsDeletedBefore(
            deletedBefore = Instant.parse("2025-06-01T00:00:00Z"),
            limit = 1,
            afterId = first.id.value,
        )

        // then
        assertEquals(listOf(second.id.value), result)
    }

    @Test
    fun `deleteByIds 성공 테스트 - 지정한 키워드만 삭제된다`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        val target = createForTest(userId, insertKeywordAndReturnId(userId, "keyword1"), TestDescription)
        val other = createForTest(userId, insertKeywordAndReturnId(userId, "keyword2"), TestDescription)

        // when
        repository.deleteByIds(listOf(target.id.value))

        // then
        assertNull(findByIdForTest(target.id.value))
        assertNotNull(findByIdForTest(other.id.value))
    }

    private suspend fun insertUserAndReturnId(uniqueValue: String): UserId = TestDatabaseFactory.dbQuery {
        val savedUser = UserEntity.new {
            this.role = Role.USER
            this.provider = SocialLoginProvider.GOOGLE
            this.providerId = "pid$uniqueValue"
            this.displayId = "did$uniqueValue"
            this.name = "name$uniqueValue"
            this.profileImageUrl = null
            this.introduce = "hello"
            this.isActive = true
            this.lastLoginAt = Instant.now()
        }
        UserId(savedUser.id.value)
    }

    private suspend fun insertKeywordAndReturnId(
        userId: UserId,
        keyword: String,
    ): KeywordId = TestDatabaseFactory.dbQuery {
        val savedKeyword = KeywordEntity.new {
            this.keyword = keyword
            this.embedding = "embedding"
            this.createdBy = EntityID(userId.value, Users)
        }
        KeywordId(savedKeyword.id.value)
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

    private suspend fun createForTest(
        userId: UserId,
        keywordId: KeywordId,
        description: Description,
    ): UserKeywordEntity = TestDatabaseFactory.dbQuery {
        UserKeywordEntity.new {
            this.keywordId = EntityID(keywordId.value, Keywords)
            this.userId = EntityID(userId.value, Users)
            this.description = description.value
        }
    }

    private suspend fun findByIdForTest(userKeywordId: Long): UserKeywordEntity? = TestDatabaseFactory.dbQuery {
        UserKeywordEntity.findById(userKeywordId)
    }

    private suspend fun findDeletedAtForTest(userKeywordId: Long): Instant? = TestDatabaseFactory.dbQuery {
        UserKeywordEntity.findById(userKeywordId)?.deletedAt
    }

    private suspend fun setSoftDeletedForTest(userKeywordId: Long, deletedAt: Instant) = TestDatabaseFactory.dbQuery {
        UserKeywords.update({ UserKeywords.id eq userKeywordId }) {
            it[isActive] = false
            it[UserKeywords.deletedAt] = deletedAt
        }
    }

    private suspend fun insertUserKeywordReport(reporterId: UserId, userKeywordId: Long) = TestDatabaseFactory.dbQuery {
        Reports.insert {
            it[Reports.reporterId] = EntityID(reporterId.value, Users)
            it[reportedUserKeywordId] = EntityID(userKeywordId, UserKeywords)
            it[reasonId] = EntityID(1L, ReportReasons) // 기존 initData에서 생성된 신고 사유
        }
    }

    companion object {
        private val TestDescription = Description("hello")
        private const val TEST_KEYWORD = "keyword"
    }
}
