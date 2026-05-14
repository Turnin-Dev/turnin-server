package com.turnin.common.db

import com.turnin.common.util.config.RunEnvironment
import com.turnin.common.util.log.AppLoggerFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
import org.jetbrains.exposed.sql.Database

/**
 * 데이터베이스 초기화(마이그레이션(Flyway) 수행, HikariCP 데이터소스 구성 포함)
 * 코루틴 기반 트랜잭션 헬퍼(dbQuery) 제공을 담당합니다.
 *
 * 주의:
 * - 트랜잭션 헬퍼(dbQuery)는 IO 전용 컨텍스트(Dispatchers.IO)에서 newSuspendedTransaction을 실행합니다.
 */
object DatabaseFactory {
    fun initialize(
        environment: RunEnvironment,
        dbUrl: String,
        dbUser: String,
        dbPassword: String,
    ): HikariDataSource = try {
        val dataSource = hikariDataSource(dbUrl, dbUser, dbPassword)
        migrate(environment, dataSource)
        Database.connect(dataSource)
        LOGGER.info("Database connection successful: ${redactJdbcUrl(dbUrl)}")
        dataSource
    } catch (e: FlywayException) {
        LOGGER.error(e, "Database connection failed: ${e.message}")
        throw e
    } catch (e: Exception) {
        LOGGER.error(e, "Database connection failed: ${e.message}")
        throw e
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
                connectionInitSql = "SET TIME ZONE 'UTC'"
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
//                flywayBuilder.cleanDisabled(false).load().also { it.clean() }
                LOGGER.warn("Dev 환경에서도 Flyway.clean()을 수행하지 않으므로 모든 스키마가 유지됩니다.")
                flywayBuilder.cleanDisabled(true).load()
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
