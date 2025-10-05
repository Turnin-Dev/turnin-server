package com.peekr.util

import com.peekr.common.db.DatabaseException
import com.peekr.common.db.schema.Friends
import com.peekr.common.db.schema.Keywords
import com.peekr.common.db.schema.RefreshTokens
import com.peekr.common.db.schema.UserKeywords
import com.peekr.common.db.schema.Users
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

/** 테스트에서 사용할 H2 DB */
object TestDatabaseFactory {
    fun init() {
        Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
            driver = "org.h2.Driver",
        )
        // 실제 테이블 모델 그대로 사용
        transaction {
            SchemaUtils.create(
                Users,
                RefreshTokens,
                Keywords,
                UserKeywords,
                Friends,
            )
        }
    }

    fun cleanUp() {
        transaction {
            SchemaUtils.drop(
                UserKeywords,
                RefreshTokens,
                Keywords,
                Friends,
                Users,
            )

            SchemaUtils.create(
                Users,
                RefreshTokens,
                Keywords,
                UserKeywords,
                Friends,
            )
        }
    }

    suspend fun <T> dbQuery(block: () -> T): T = newSuspendedTransaction {
        try {
            block()
        } catch (e: ExposedSQLException) {
            throw DatabaseException.DBQueryException(e)
        } catch (e: Exception) {
            throw e
        }
    }
}
