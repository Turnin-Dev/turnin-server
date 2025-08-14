package com.peekr.common.db

import com.peekr.common.util.config.RunEnvironment
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.util.logging.KtorSimpleLogger
import java.sql.SQLException
import javax.sql.DataSource
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/** 데이터베이스를 초기화하고 전용 쿼리 메서드를 제공한다. */
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
            LOGGER.info("Database connection successfully: $dbUrl")
        } catch (e: FlywayException) {
            LOGGER.error("Database migration failed: ${e.message}")
            throw e // 마이그레이션 실패 시에는 앱이 동작하지 않게끔 설정
        } catch (e: Exception) {
            LOGGER.error("Database connection failed: ${e.message}")
        }
    }

    suspend fun <T> dbQuery(block: () -> T): T = newSuspendedTransaction(ioContext) {
        try {
            block()
        } catch (e: ExposedSQLException) {
            throw DatabaseException.DBQueryException(e.message)
        } catch (e: SQLException) {
            throw DatabaseException.DBQueryException(e.message)
        } catch (e: Exception) {
            throw e
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
                flywayBuilder.cleanDisabled(false).load().also { it.clean() }
            }

            RunEnvironment.Prod -> {
                flywayBuilder.cleanDisabled(true).load()
            }
        }

        flyway.migrate()
    }
}

private val LOGGER = KtorSimpleLogger("Database")
