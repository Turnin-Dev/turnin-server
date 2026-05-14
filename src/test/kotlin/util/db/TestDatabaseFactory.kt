package com.turnin.util.db

import com.turnin.common.db.DatabaseException
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
import com.turnin.common.model.FriendRequestStatus
import com.turnin.common.model.NotificationType
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
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
            // H2에서 커스텀 ENUM 타입 생성
            val enums = mapOf(
                "user_role" to Role.entries.map { it.name },
                "social_login_provider" to SocialLoginProvider.entries.map { it.name },
                "friend_status" to FriendRequestStatus.entries.map { it.name },
                "noti_type" to NotificationType.entries.map { it.name },
            )

            enums.forEach { (typeName, values) ->
                val valuesString = values.joinToString { "'$it'" }
                // H2 문법에 맞게 생성 (IF NOT EXISTS 포함)
                exec("CREATE TYPE IF NOT EXISTS $typeName AS ENUM ($valuesString);")
            }

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
            )

            initData()
        }
    }

    fun cleanUp() {
        transaction {
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
            )

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
