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
        // 공통 기본 설정 구성
        val config = Flyway
            .configure()
            .dataSource(dataSource)
            .schemas("public")
            .locations("classpath:db/migration")

        // 환경별 동적 설정 분기
        val flyway = when (env) {
            RunEnvironment.Dev -> {
                // 로컬/개발 환경에서 필요 시 clean 기능을 켤 수 있도록 유연성 부여
                // (현재는 주석 처리된 clean 로직에 맞춰 false로 안전장치만 해제하거나 true 유지 가능)
                // flywayBuilder.cleanDisabled(false).load().also { it.clean() }
                LOGGER.warn("Dev 환경에서도 Flyway.clean()을 수행하지 않으므로 모든 스키마가 유지됩니다.")
                config.cleanDisabled(true).load()
            }

            RunEnvironment.Prod -> {
                // 운영 환경이므로 clean 금지
                config.cleanDisabled(true).load()
            }
        }

        // 마이그레이션 수행
        try {
            val result = flyway.migrate()
            if (result.migrationsExecuted > 0) {
                LOGGER.info(
                    "Flyway 마이그레이션 성공: ${result.migrationsExecuted}개의 파일이 적용되었습니다." +
                        "(버전: ${result.targetSchemaVersion})",
                )
            } else {
                LOGGER.info("Flyway 마이그레이션 체크 완료: 적용할 새로운 파일이 없습니다.")
            }
        } catch (e: Exception) {
            LOGGER.error(e, "Flyway 마이그레이션 수행 중 에러가 발생했습니다.")
            throw e // 서버 구동 중단
        }
    }
}

private val LOGGER = AppLoggerFactory.createLogger("DatabaseFactory")

private fun redactJdbcUrl(url: String): String =
    url
        .replace(Regex("(?i)(password|pwd|pass)=([^&;]+)"), "$1=***")
        .replace(Regex("(?i)://([^:/@]+):([^@]+)@"), "://$1:***@")
