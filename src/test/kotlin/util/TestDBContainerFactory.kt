package com.peekr.util

import com.peekr.common.db.schema.Keywords
import com.peekr.common.db.schema.UserKeywords
import com.peekr.common.db.schema.Users
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.vendors.PostgreSQLDialect
import org.jetbrains.exposed.sql.vendors.currentDialect
import org.testcontainers.containers.PostgreSQLContainer

/**
 * 실제 PostgreSQL 도커 컨테이너를 사용하여 테스트하기 위해 사용한다.
 */
object TestDBContainerFactory {
    private val container = PostgreSQLContainer<Nothing>("pgvector/pgvector:pg16").apply {
        withDatabaseName("testdb2")
        withUsername("test")
        withPassword("test")
        start()
    }

    fun init() {
        Database.connect(
            url = container.jdbcUrl,
            driver = "org.postgresql.Driver",
            user = container.username,
            password = container.password,
        )

        transaction {
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
        transaction {
            SchemaUtils.drop(Users, Keywords, UserKeywords)
            SchemaUtils.create(Users, Keywords, UserKeywords)
        }
    }
}
