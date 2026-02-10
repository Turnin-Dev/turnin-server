package com.peekr.util.db

import com.peekr.common.db.DatabaseException
import com.peekr.common.db.schema.BlockReasons
import com.peekr.common.db.schema.Blocks
import com.peekr.common.db.schema.Friends
import com.peekr.common.db.schema.Keywords
import com.peekr.common.db.schema.RefreshTokens
import com.peekr.common.db.schema.ReportReasons
import com.peekr.common.db.schema.Reports
import com.peekr.common.db.schema.UserKeywords
import com.peekr.common.db.schema.Users
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * 테스트에서 사용할 H2 DB
 *
 * 대부분의 테스트에서 사용된다.
 */
object TestDatabaseFactory {
    fun init() {
        Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
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
                ReportReasons,
                Reports,
                BlockReasons,
                Blocks,
            )

            initData()
        }
    }

    fun cleanUp() {
        transaction {
            Blocks.deleteAll()
            Reports.deleteAll()
            UserKeywords.deleteAll()
            Friends.deleteAll()
            RefreshTokens.deleteAll()
            Keywords.deleteAll()
            Users.deleteAll()
            ReportReasons.deleteAll()
            BlockReasons.deleteAll()

            initData()
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

    private fun initData() {
        // 초기 데이터 삽입
        repeat(2) {
            BlockReasons.insertIgnore { stmt ->
                stmt[code] = "TEST_BLOCK_REASON_$it"
                stmt[description] = "TEST_BLOCK_REASON_DESC_$it"
            }
        }
    }
}
