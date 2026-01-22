package com.peekr.domain.userKeyword.application.usecase.integration

import com.peekr.common.db.schema.KeywordEntity
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.db.schema.Users
import com.peekr.common.db.suspendTransaction
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.domain.userKeyword.application.dto.UserKeywordPatchDto
import com.peekr.domain.userKeyword.application.usecase.UpdateUserKeywordUseCase
import com.peekr.domain.userKeyword.infrastructure.repository.impl.UserKeywordRepositoryImpl
import com.peekr.util.db.TestDatabaseFactory
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.dao.id.EntityID
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull

class UpdateUserKeywordUseCaseIntegrationTest {
    private val userKeywordRepository = UserKeywordRepositoryImpl()
    private val keywordProvider = FakeKeywordProvider()
    private val usecase = UpdateUserKeywordUseCase(userKeywordRepository, keywordProvider)

    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @After
    fun teardown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `사용자 키워드 업데이트에 실패하는 경우 롤백을 수행한다`() = runTest {
        // given: 데이터 준비
        val userId = insertUserAndReturnId()
        val keywordId = insertKeywordAndReturnId(userId, "keyword")
        assertNotNull(keywordProvider.findById(keywordId))

        val newKeyword = "newKeyword"
        val patch = UserKeywordPatchDto(
            userKeywordId = 1L,
            keywordName = newKeyword,
            description = "newDescription",
        )

        // when: 사용자 키워드가 없는 경우 롤백을 수행해서 키워드 저장이 취소된다.
        runCatching {
            usecase(userId.value, patch)
        }

        // then: 결과는 실패했으므로 false를 반환하고 키워드는 존재하지 않아야 한다.
        assertNull(keywordProvider.findByName(newKeyword))
    }

    private suspend fun insertUserAndReturnId(): UserId = suspendTransaction {
        val savedUser = UserEntity.new {
            this.role = Role.USER
            this.provider = SocialLoginProvider.GOOGLE
            this.providerId = "asdasdads"
            this.displayId = "hong"
            this.name = "honggd"
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
    ): KeywordId = suspendTransaction {
        val savedKeyword = KeywordEntity.new {
            this.keyword = keyword
            this.embedding = "embedding"
            this.createdBy = EntityID(userId.value, Users)
        }
        KeywordId(savedKeyword.id.value)
    }
}
