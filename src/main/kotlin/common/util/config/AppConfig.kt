package com.peekr.common.util.config

import io.ktor.server.config.ApplicationConfig
import org.koin.core.annotation.Singleton

@Singleton
class AppConfig {
    val applicationConfiguration: ApplicationConfig = ApplicationConfig("application.conf")
}
