package com.turnin.domain.auth.application.usecase

import com.turnin.common.db.schema.RefreshTokens
import com.turnin.common.db.schema.UserEntity
import com.turnin.common.jwt.JWTTestDoubles.ACCESS_TOKEN_EXPIRES_IN
import com.turnin.common.jwt.JWTTestDoubles.AUDIENCE
import com.turnin.common.jwt.JWTTestDoubles.ISSUER
import com.turnin.common.jwt.JWTTestDoubles.REALM
import com.turnin.common.jwt.JWTTestDoubles.REFRESH_TOKEN_EXPIRES_IN
import com.turnin.common.jwt.JWTTestDoubles.SECRET
import com.turnin.common.jwt.domain.service.JWTTokenService
import com.turnin.common.jwt.infrastructure.JWTTokenServiceImpl
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.id.UserId
import com.turnin.common.util.config.AppConfig
import com.turnin.domain.auth.application.dto.LoginDto
import com.turnin.domain.auth.application.dto.LoginResultDto
import com.turnin.domain.auth.domain.repository.RefreshTokenRepository
import com.turnin.domain.auth.exception.AuthException
import com.turnin.domain.auth.infrastructure.repository.impl.AuthRepositoryImpl
import com.turnin.domain.auth.infrastructure.repository.impl.RefreshTokenRepositoryImpl
import com.turnin.util.db.TestDatabaseFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.selectAll
import org.junit.Test

/**
 * 로그인 유스케이스 트랜잭션 일관성 테스트
 *
 * [LoginUseCase]의 트랜잭션 원자성을 검증한다.
 *
 * 최소한의 Mock을 사용하고 최대한 실제 데이터를 통해 테스트한다.
 */
class LoginUseCaseTransactionTest {
    private val appConfigManager: AppConfig = mockk()
    private val refreshTokenRepository: RefreshTokenRepository = mockk()
    private lateinit var jwtTokenService: JWTTokenService
    private val authRepository = AuthRepositoryImpl()
    private lateinit var usecase: LoginUseCase

    @BeforeTest
    fun setup() {
        // DB Init
        TestDatabaseFactory.init()

        // Mock
        every { appConfigManager.get(any()) } answers {
            val key = firstArg<String>()
            when (key) {
                "ktor.security.jwt.realm" -> REALM
                "ktor.security.jwt.issuer" -> ISSUER
                "ktor.security.jwt.audience" -> AUDIENCE
                "ktor.security.jwt.secret" -> SECRET
                "ktor.security.jwt.accessTokenExpiresIn" -> ACCESS_TOKEN_EXPIRES_IN.toString()
                "ktor.security.jwt.refreshTokenExpiresIn" -> REFRESH_TOKEN_EXPIRES_IN.toString()
                else -> "default-value"
            }
        }

        every { appConfigManager.getOrDefault(any(), any()) } answers {
            val key = firstArg<String>()
            val defaultValue = secondArg<String>()
            when (key) {
                "ktor.security.jwt.realm" -> REALM
                "ktor.security.jwt.issuer" -> ISSUER
                "ktor.security.jwt.audience" -> AUDIENCE
                "ktor.security.jwt.secret" -> SECRET
                "ktor.security.jwt.accessTokenExpiresIn" -> ACCESS_TOKEN_EXPIRES_IN.toString()
                "ktor.security.jwt.refreshTokenExpiresIn" -> REFRESH_TOKEN_EXPIRES_IN.toString()
                else -> defaultValue
            }
        }

        jwtTokenService = JWTTokenServiceImpl(appConfigManager, SECRET)
        usecase = LoginUseCase(authRepository, refreshTokenRepository, jwtTokenService)
    }

    @AfterTest
    fun teardown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `트랜잭션 원자성 테스트 - 리프레쉬 토큰 저장 실패 시 lastLoginAt도 롤백되어야 한다`() = runTest {
        // given
        val userId = insertUserAndReturnId()
        val initialLastLoginAt = TestDatabaseFactory.dbQuery {
            UserEntity.findById(userId.value)?.lastLoginAt
        }
        assertNotNull(initialLastLoginAt)

        coEvery {
            refreshTokenRepository.save(userId, any())
        } returns false

        // when
        val exception = runCatching { usecase(TestLoginDto) }.exceptionOrNull()
        val finalLastLoginAt = TestDatabaseFactory.dbQuery {
            UserEntity.findById(userId.value)?.lastLoginAt
        }

        // then
        assertTrue(
            exception is AuthException,
            "리프레쉬 토큰 로직 실패 시 알려진 예외가 발생해야 한다.",
        )
        assertEquals(
            initialLastLoginAt,
            finalLastLoginAt,
            "리프레쉬 토큰 저장이 실패했으므로 lastLoginAt이 롤백되어야 한다",
        )
        coVerify { refreshTokenRepository.save(userId, any()) }
    }

    @Test
    fun `트랜잭션 롤백 테스트 - 예외 발생 시`() = runTest {
        // given
        val userId = insertUserAndReturnId()
        val initialLastLoginAt = TestDatabaseFactory.dbQuery {
            UserEntity.findById(userId.value)?.lastLoginAt
        }
        assertNotNull(initialLastLoginAt)

        coEvery {
            refreshTokenRepository.save(userId, any())
        } throws Exception("database error")

        // when
        val result = runCatching { usecase(TestLoginDto) }
            .exceptionOrNull()
        val finalLastLoginAt = TestDatabaseFactory.dbQuery {
            UserEntity.findById(userId.value)?.lastLoginAt
        }
        val tokenCount = TestDatabaseFactory.dbQuery {
            RefreshTokens.selectAll().count()
        }

        // then
        assertNotNull(result)
        assertEquals(
            initialLastLoginAt,
            finalLastLoginAt,
            "예외 발생 시 lastLoginAt이 롤백되어야 한다",
        )
        assertEquals(
            0,
            tokenCount,
            "예외 발생 시 리프레쉬 토큰이 저장되지 않아야 한다",
        )
    }

    @Test
    fun `트랜잭션 커밋 확인 - 모든 작업 성공 시`() = runTest {
        // given
        val userId = insertUserAndReturnId()
        val initialLastLoginAt = TestDatabaseFactory.dbQuery {
            UserEntity.findById(userId.value)?.lastLoginAt
        }
        val refreshTokenRepository = RefreshTokenRepositoryImpl()
        usecase = LoginUseCase(authRepository, refreshTokenRepository, jwtTokenService)

        // when
        val result = usecase(TestLoginDto)
        val finalLastLoginAt = TestDatabaseFactory.dbQuery {
            UserEntity.findById(userId.value)?.lastLoginAt
        }
        val savedToken = TestDatabaseFactory.dbQuery {
            RefreshTokens
                .select(RefreshTokens.user)
                .where { RefreshTokens.user eq userId.value }
                .singleOrNull()
        }

        // then
        assertNotNull(result)
        assertEquals(userId, result.userId)
        assertNotEquals(
            initialLastLoginAt,
            finalLastLoginAt,
            "마지막 로그인 일자가 달라야 한다.",
        )
        assertNotNull(
            savedToken,
            "리프레쉬 토큰도 저장되어야 한다.",
        )
    }

    @Test
    fun `트랜잭션 격리 테스트 - 동시 로그인 시도시 토큰은 하나만 저장되어야 한다`() = runTest {
        // given
        insertUserAndReturnId()
        val refreshTokenRepository = RefreshTokenRepositoryImpl()
        usecase = LoginUseCase(authRepository, refreshTokenRepository, jwtTokenService)

        // when: 동시에 여러 개의 로그인 시도
        val latch = CountDownLatch(3)
        val results = mutableListOf<LoginResultDto?>()
        val exceptions = mutableListOf<Throwable>()

        repeat(3) {
            thread {
                try {
                    val result = runBlocking {
                        usecase(TestLoginDto)
                    }
                    synchronized(results) {
                        results.add(result)
                    }
                } catch (e: Throwable) {
                    synchronized(exceptions) {
                        exceptions.add(e)
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()

        val tokenCount = TestDatabaseFactory.dbQuery {
            RefreshTokens.selectAll().count()
        }

        // then: 모든 로그인이 성공하거나 예외가 발생해야 한다.
        assertTrue(results.all { it != null } || exceptions.isNotEmpty())
        assertEquals(
            1,
            tokenCount,
            "내부적으로 upsert하므로 리프레쉬 토큰은 하나만 저장되어야 한다.",
        )
    }

    private suspend fun insertUserAndReturnId(): UserId = TestDatabaseFactory.dbQuery {
        val savedUser = UserEntity.new {
            this.role = Role.USER
            this.provider = SocialLoginProvider.GOOGLE
            this.providerId = TEST_PROVIDER_ID
            this.displayId = "hong"
            this.name = "honggd"
            this.profileImageUrl = null
            this.introduce = "hello"
            this.isActive = true
            this.lastLoginAt = Instant.now()
        }
        UserId(savedUser.id.value)
    }

    companion object {
        private const val TEST_PROVIDER_ID = "provider-id"
        private val TestLoginDto = LoginDto(
            provider = SocialLoginProvider.GOOGLE,
            providerId = TEST_PROVIDER_ID,
        )
    }
}
