package com.peekr

import com.peekr.common.di.configureKoin
import com.peekr.common.exception.configureExceptionHandler
import com.peekr.common.jwt.configureJwtSecurity
import com.peekr.common.jwt.di.jwtModule
import com.peekr.common.plugin.DatabaseFactory
import com.peekr.common.plugin.configureCallLogging
import com.peekr.common.plugin.configureContentNegotiation
import com.peekr.common.plugin.configureHTTP
import com.peekr.common.plugin.configureRouting
import com.peekr.common.plugin.configureSerialization
import com.peekr.domain.auth.di.authModule
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init()

    configureKoin {
        modules(
            authModule,
            jwtModule,
        )
    }

    configureContentNegotiation()
    configureExceptionHandler()
    configureCallLogging()

    configureHTTP()
    configureJwtSecurity()
    configureSerialization()
    configureRouting()
}
