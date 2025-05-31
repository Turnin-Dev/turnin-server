package com.peekr

import com.peekr.presentation.exception.configureExceptionHandler
import com.peekr.presentation.plugin.DatabaseFactory
import com.peekr.presentation.plugin.configureCallLogging
import com.peekr.presentation.plugin.configureContentNegotiation
import com.peekr.presentation.plugin.configureHTTP
import com.peekr.presentation.plugin.configureKoin
import com.peekr.presentation.plugin.configureRouting
import com.peekr.presentation.plugin.configureSecurity
import com.peekr.presentation.plugin.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init()
    configureContentNegotiation()
    configureExceptionHandler()
    configureCallLogging()

    configureHTTP()
    configureSecurity()
    configureSerialization()
    configureRouting()

    configureKoin {
    }
}
