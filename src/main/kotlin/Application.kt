package com.peekr

import com.peekr.config.configureCallLogging
import com.peekr.config.configureContentNegotiation
import com.peekr.config.configureDatabases
import com.peekr.config.configureHTTP
import com.peekr.config.configureKoin
import com.peekr.config.configureRouting
import com.peekr.config.configureSecurity
import com.peekr.config.configureSerialization
import com.peekr.config.exception.configureExceptionHandler
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureContentNegotiation()
    configureDatabases()
    configureExceptionHandler()
    configureCallLogging()

    configureHTTP()
    configureSecurity()
    configureSerialization()
    configureRouting()

    configureKoin {
    }
}
