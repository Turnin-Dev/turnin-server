package com.turnin.common.jwt.di

import com.turnin.common.jwt.domain.service.JWTTokenService
import com.turnin.common.jwt.infrastructure.JWTTokenServiceImpl
import com.turnin.common.util.config.AppConfig
import org.koin.core.qualifier.named
import org.koin.dsl.module

val jwtModule = module {
    single<JWTTokenService>(named("user")) {
        val appConfig = get<AppConfig>()

        JWTTokenServiceImpl(
            appConfig = appConfig,
            secretKey = appConfig
                .get("ktor.security.jwt.secret")
                ?.takeIf { it.isNotBlank() }
                ?: error("User JWT secret key is not configured or blank"),
        )
    }

    single<JWTTokenService>(named("admin")) {
        val appConfig = get<AppConfig>()

        JWTTokenServiceImpl(
            appConfig = appConfig,
            secretKey = appConfig
                .get("ktor.admin.jwtSecretKey")
                ?.takeIf { it.isNotBlank() }
                ?: error("Admin JWT secret key is not configured or blank"),
        )
    }
}
