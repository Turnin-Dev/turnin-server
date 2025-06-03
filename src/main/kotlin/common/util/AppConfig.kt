package com.peekr.common.util

import io.ktor.server.config.ApplicationConfig
import org.koin.core.annotation.Singleton

@Singleton
class AppConfig {
    val applicationConfiguration: ApplicationConfig = ApplicationConfig("application.yaml")
}
