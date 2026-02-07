package com.peekr

import com.peekr.ApplicationUtils.initDatabase
import com.peekr.ApplicationUtils.printSection
import com.peekr.ApplicationUtils.printServerSettings
import com.peekr.ApplicationUtils.printTimeZone
import com.peekr.common.di.configureKoin
import com.peekr.common.exception.configureExceptionHandler
import com.peekr.common.jwt.configureJwtSecurity
import com.peekr.common.ml.embeddingResourceAutoCleanup
import com.peekr.common.plugin.configureAPIDocuments
import com.peekr.common.plugin.configureCallLogging
import com.peekr.common.plugin.configureContentNegotiation
import com.peekr.common.plugin.configureCors
import com.peekr.common.plugin.configureResources
import com.peekr.common.plugin.configureRouting
import com.peekr.common.util.getTimeZoneInfo
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.netty.EngineMain
import java.util.TimeZone
import kotlinx.coroutines.delay

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    intercept(ApplicationCallPipeline.Call) {
        delay(2000L)
        proceed()
    }

    // ------------------------------ Setting ------------------------------
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    configureKoin()

    // ------------------------------ Initialize ------------------------------
    initDatabase()

    // ------------------------------ Plugins ------------------------------
    configureResources()
    configureCors()

    configureContentNegotiation()
    configureExceptionHandler()
    configureCallLogging()

    configureJwtSecurity()

    configureAPIDocuments()
    configureRouting()

    // ------------------------------ Print ------------------------------
    val timeZoneInfo = getTimeZoneInfo()
    printSection {
        printServerSettings()
        printTimeZone(timeZoneInfo)
    }

    // ------------------------------ Embedding Service cleanup ------------------------------
    embeddingResourceAutoCleanup()
}
