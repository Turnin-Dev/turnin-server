package com.turnin.common.db

import com.turnin.common.util.config.AppConfig
import com.turnin.common.util.config.RunEnvironment.Companion.toRunEnvironment
import com.turnin.common.util.log.AppLoggerFactory
import com.zaxxer.hikari.HikariDataSource
import org.koin.dsl.module
import org.koin.dsl.onClose

val databaseModule = module {
    single<HikariDataSource> {
        val appConfig = get<AppConfig>()
        val environment = appConfig.getOrDefault("ktor.environment", "dev").toRunEnvironment()
        val dbUrl = appConfig.getRequired("ktor.db.url")
        val dbUser = appConfig.getRequired("ktor.db.user")
        val dbPassword = appConfig.getRequired("ktor.db.password")

        DatabaseFactory.initialize(environment, dbUrl, dbUser, dbPassword)
    } onClose {
        try {
            LOGGER.info("Closing Database...")
            it?.close()
            LOGGER.info("Database closed successfully")
        } catch (e: Exception) {
            LOGGER.error(e, "Failed to close Database")
        }
    }
}

private val LOGGER = AppLoggerFactory.createLogger("DatabaseModule")
