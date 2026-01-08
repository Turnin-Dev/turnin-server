package com.peekr.util.db

import com.peekr.common.db.DatabaseException
import com.peekr.common.db.schema.Keywords
import com.peekr.common.db.schema.UserKeywords
import com.peekr.common.db.schema.Users
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
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
            SchemaUtils.create(Users, Keywords, UserKeywords)
        }
    }

    fun cleanUp() {
        transaction(database) {
            SchemaUtils.drop(Users, Keywords, UserKeywords)
            SchemaUtils.create(Users, Keywords, UserKeywords)
        }
    }

    fun shutdown() {
        container.stop()
        database = null
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
