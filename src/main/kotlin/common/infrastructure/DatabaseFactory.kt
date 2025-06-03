package com.peekr.common.infrastructure

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.cdimascio.dotenv.dotenv
import io.ktor.util.logging.KtorSimpleLogger
import javax.sql.DataSource
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object DatabaseFactory {
    private val ioContext: CoroutineContext = Dispatchers.IO
    private val dotenv = dotenv()

    private val dbUrl = dotenv["DB_URL"] ?: "jdbc:postgresql://localhost:5432/defaultdb"
    private val dbUser = dotenv["DB_USER"] ?: "defaultuser"
    private val dbPassword = dotenv["DB_PASSWORD"] ?: "defaultpassword"

    fun init() {
        try {
            val dataSource = hikariDataSource()
            migrate(dataSource)
            Database.connect(dataSource)
            LOGGER.info("Database connection successfully: $dbUrl")
        } catch (e: FlywayException) {
            LOGGER.error("Database migration failed: ${e.message}")
            throw e // 마이그레이션 실패 시에는 앱이 동작하지 않게끔 설정
        } catch (e: Exception) {
            LOGGER.error("Database connection failed: ${e.message}")
        }
//    //TODO: 추후 삭제 예정
//    transaction {
//        SchemaUtils.drop(Users) // 데이터베이스 초기화 (개발 중에만 사용)
//        SchemaUtils.create(Users) // Users 테이블 생성
//    }
    }

    suspend fun <T> dbQuery(block: () -> T): T = newSuspendedTransaction(ioContext) { block() }

    private fun hikariDataSource(): HikariDataSource =
        HikariDataSource(
            HikariConfig().apply {
                driverClassName = "org.postgresql.Driver"
                jdbcUrl = dbUrl
                username = dbUser
                password = dbPassword
                maximumPoolSize = 5
//            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            },
        )

    private fun migrate(dataSource: DataSource) {
        val flyway =
            Flyway
                .configure()
                .dataSource(dataSource)
//                    .locations("classpath:db/migration") // 필요 시 명시
                .baselineOnMigrate(true) // 기존 DB에 적용 시 필요
                .load()
        flyway.migrate()
    }
}

private val LOGGER = KtorSimpleLogger("Database")
