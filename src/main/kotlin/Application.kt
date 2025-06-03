package com.peekr

import com.peekr.common.di.configureKoin
import com.peekr.common.infrastructure.DatabaseFactory
import com.peekr.common.presentation.exception.configureExceptionHandler
import com.peekr.common.presentation.plugin.configureCallLogging
import com.peekr.common.presentation.plugin.configureContentNegotiation
import com.peekr.common.presentation.plugin.configureHTTP
import com.peekr.common.presentation.plugin.configureRouting
import com.peekr.common.presentation.plugin.configureSerialization
import com.peekr.common.presentation.plugin.security.configureSecurity
import com.peekr.domain.auth.di.authModule
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init()

    configureKoin {
        modules(authModule)
    }

    configureContentNegotiation()
    configureExceptionHandler()
    configureCallLogging()

    configureHTTP()
    configureSecurity()
    configureSerialization()
    configureRouting()
}
