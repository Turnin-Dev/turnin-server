package com.peekr.common.db

import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.config.AppConfig
import com.peekr.common.util.config.RunEnvironment.Companion.toRunEnvironment
import com.zaxxer.hikari.HikariDataSource
import org.koin.dsl.module
import org.koin.dsl.onClose

val databaseModule = module {
    single<HikariDataSource> {
        val appConfig = get<AppConfig>()
        val environment = appConfig.getOrDefault("ktor.environment", "dev").toRunEnvironment()
        val dbUrl = appConfig.getOrDefault("ktor.db.url", "jdbc:postgresql://localhost:5432/defaultdb")
        val dbUser = appConfig.getOrDefault("ktor.db.user", "defaultuser")
        val dbPassword = appConfig.getOrDefault("ktor.db.password", "defaultpassword")

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
