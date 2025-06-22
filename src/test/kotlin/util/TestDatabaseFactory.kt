package com.peekr.util

import com.peekr.common.db.DatabaseException
import com.peekr.common.db.scheme.Users
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
        transaction {
            SchemaUtils.drop(Users)
            SchemaUtils.create(Users) // 실제 테이블 모델 그대로 사용
        }
    }

    suspend fun <T> dbQuery(block: () -> T): T = newSuspendedTransaction {
        try {
            block()
        } catch (e: ExposedSQLException) {
            throw DatabaseException.DBQueryException(e.message)
        } catch (e: Exception) {
            throw e
        }
    }
}
