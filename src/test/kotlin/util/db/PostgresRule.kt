package com.turnin.util.db

import com.turnin.common.db.DatabaseException
import com.turnin.common.db.schema.AnnouncementReads
import com.turnin.common.db.schema.Announcements
import com.turnin.common.db.schema.BlockReasons
import com.turnin.common.db.schema.Blocks
import com.turnin.common.db.schema.Friends
import com.turnin.common.db.schema.Keywords
import com.turnin.common.db.schema.Notifications
import com.turnin.common.db.schema.RefreshTokens
import com.turnin.common.db.schema.ReportReasons
import com.turnin.common.db.schema.Reports
import com.turnin.common.db.schema.UserFcmTokens
import com.turnin.common.db.schema.UserKeywords
import com.turnin.common.db.schema.Users
import com.turnin.common.model.AnnouncementAudience
import com.turnin.common.model.AnnouncementStatus
import com.turnin.common.model.FriendRequestStatus
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.vendors.PostgreSQLDialect
import org.jetbrains.exposed.sql.vendors.currentDialect
import org.junit.rules.ExternalResource
import org.testcontainers.containers.PostgreSQLContainer

class PostgresRule : ExternalResource() {
    override fun before() {
        TestDBContainerFactory.init()
    }

    override fun after() {
        TestDBContainerFactory.cleanUp()
        TestDBContainerFactory.shutdown()
    }

    suspend fun <T> dbQuery(block: () -> T): T =
        TestDBContainerFactory.dbQuery(block)
}

/**
 * 실제 PostgreSQL 도커 컨테이너를 사용하여 테스트하기 위해 사용한다.
 *
 * 실제 DB 작업이 필요한 테스트에서 극히 일부 사용한다.
 */
private object TestDBContainerFactory {
    private var database: Database? = null
    private lateinit var container: PostgreSQLContainer<Nothing>

    fun init() {
        if (database != null) return

        container = PostgreSQLContainer<Nothing>("pgvector/pgvector:pg16").apply {
            withDatabaseName("testdb2")
            withUsername("test")
            withPassword("test")
            start()
        }

        database = Database.connect(
            url = container.jdbcUrl,
            driver = "org.postgresql.Driver",
            user = container.username,
            password = container.password,
        )

        transaction(database) {
            // pgvector 확장 기능 활성화 (매우 중요)
            exec("CREATE EXTENSION IF NOT EXISTS vector;")

            // PostgreSQL인 경우에만 커스텀 ENUM 타입들 생성
            if (currentDialect is PostgreSQLDialect) {
                val enums = mapOf(
                    "user_role" to Role.entries.map { it.name },
                    "social_login_provider" to SocialLoginProvider.entries.map { it.name },
                    "friend_status" to FriendRequestStatus.entries.map { it.name },
                    "announcement_audience" to AnnouncementAudience.entries.map { it.name },
                    "announcement_status" to AnnouncementStatus.entries.map { it.name },
                )

                enums.forEach { (typeName, values) ->
                    val valuesString = values.joinToString { "'$it'" }
                    exec(
                        """
                    DO $$
                    BEGIN
                        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = '$typeName') THEN
                            CREATE TYPE $typeName AS ENUM ($valuesString);
                        END IF;
                    END $$;
                        """.trimIndent(),
                    )
                }
            }

            // 테이블 생성
            SchemaUtils.create(
                Users,
                RefreshTokens,
                Keywords,
                UserKeywords,
                Friends,
                ReportReasons,
                Reports,
                BlockReasons,
                Blocks,
                UserFcmTokens,
                Notifications,
                Announcements,
                AnnouncementReads,
            )

            // 초기 데이터 준비
            initData()
        }
    }

    fun cleanUp() {
        transaction(database) {
            SchemaUtils.drop(
                Users,
                RefreshTokens,
                Keywords,
                UserKeywords,
                Friends,
                ReportReasons,
                Reports,
                BlockReasons,
                Blocks,
                UserFcmTokens,
                Notifications,
                Announcements,
                AnnouncementReads,
            )
            SchemaUtils.create(
                Users,
                RefreshTokens,
                Keywords,
                UserKeywords,
                Friends,
                ReportReasons,
                Reports,
                BlockReasons,
                Blocks,
                UserFcmTokens,
                Notifications,
                Announcements,
                AnnouncementReads,
            )

            // 초기 데이터 준비
            initData()
        }
    }

    fun shutdown() {
        container.stop()
        database = null
    }

    private fun initData() {
        // 초기 데이터 삽입
        repeat(2) {
            BlockReasons.insertIgnore { stmt ->
                stmt[code] = "TEST_BLOCK_REASON_$it"
                stmt[description] = "TEST_BLOCK_REASON_DESC_$it"
            }
        }

        repeat(2) {
            ReportReasons.insertIgnore { stmt ->
                stmt[code] = "TEST_REPORT_REASON_$it"
                stmt[description] = "TEST_REPORT_REASON_DESC_$it"
            }
        }
    }

    suspend fun <T> dbQuery(block: () -> T): T =
        newSuspendedTransaction(db = database) {
            try {
                block()
            } catch (e: ExposedSQLException) {
                throw DatabaseException.DBQueryException(e)
            } catch (e: Exception) {
                throw e
            }
        }
}
