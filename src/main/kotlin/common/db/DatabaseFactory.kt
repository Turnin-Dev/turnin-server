package com.peekr.common.db

import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.config.RunEnvironment
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.SQLException
import javax.sql.DataSource
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * 데이터베이스 초기화(마이그레이션(Flyway) 수행, HikariCP 데이터소스 구성 포함)
 * 코루틴 기반 트랜잭션 헬퍼(dbQuery) 제공을 담당합니다.
 *
 * 주의:
 * - Dev 환경에서는 migrate 시 clean()을 호출하여 기존 스키마가 초기화됩니다.
 * - 트랜잭션 헬퍼(dbQuery)는 IO 전용 컨텍스트(Dispatchers.IO)에서 newSuspendedTransaction을 실행합니다.
 */
object DatabaseFactory {
    private val ioContext: CoroutineContext = Dispatchers.IO

    fun initialize(
        environment: RunEnvironment,
        dbUrl: String,
        dbUser: String,
        dbPassword: String,
    ) {
        try {
            val dataSource = hikariDataSource(dbUrl, dbUser, dbPassword)
            migrate(environment, dataSource)
            Database.connect(dataSource)
            LOGGER.info("Database connection successful: ${redactJdbcUrl(dbUrl)}")
        } catch (e: FlywayException) {
            LOGGER.error(e, "Database connection failed: ${e.message}")
            throw e
        } catch (e: Exception) {
            LOGGER.error(e, "Database connection failed: ${e.message}")
            throw e
        }
    }

    /**
     * DB 작업을 수행할 때 항상 이 범위 내에서 수행한다.
     *
     * @throws
     */
    suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(ioContext) {
        try {
            block()
        } catch (e: SQLException) {
            LOGGER.error(e, "Database query failed: ${e.message}")
            throw DatabaseException.DBQueryException(e)
        } catch (e: ExposedSQLException) {
            LOGGER.error(e, "Database query failed: ${e.message}")
            throw DatabaseException.DBQueryException(e)
        }
    }

    private fun hikariDataSource(
        dbUrl: String,
        dbUser: String,
        dbPassword: String,
    ): HikariDataSource =
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

    private fun migrate(env: RunEnvironment, dataSource: DataSource) {
        val flywayBuilder = Flyway
            .configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration") // 필요 시 명시
            .baselineOnMigrate(true) // 기존 DB에 적용 시 필요

        val flyway = when (env) {
            RunEnvironment.Dev -> {
                LOGGER.warn("Dev 환경에서 Flyway.clean()을 수행합니다. 모든 스키마가 초기화됩니다.")
                flywayBuilder.cleanDisabled(false).load().also { it.clean() }
            }

            RunEnvironment.Prod -> {
                flywayBuilder.cleanDisabled(true).load()
            }
        }

        flyway.migrate()
    }
}

private val LOGGER = AppLoggerFactory.createLogger("DatabaseFactory")

private fun redactJdbcUrl(url: String): String =
    url
        .replace(Regex("(?i)(password|pwd|pass)=([^&;]+)"), "$1=***")
        .replace(Regex("(?i)://([^:/@]+):([^@]+)@"), "://$1:***@")
