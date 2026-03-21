package com.peekr

import com.peekr.ApplicationUtils.initDatabase
import com.peekr.ApplicationUtils.printSection
import com.peekr.ApplicationUtils.printServerSettings
import com.peekr.ApplicationUtils.printTimeZone
import com.peekr.common.di.configureKoin
import com.peekr.common.exception.configureExceptionHandler
import com.peekr.common.firebase.FirebaseAdmin
import com.peekr.common.jwt.configureJwtSecurity
import com.peekr.common.ml.embeddingResourceAutoCleanup
import com.peekr.common.plugin.configureAPIDocuments
import com.peekr.common.plugin.configureCallLogging
import com.peekr.common.plugin.configureContentNegotiation
import com.peekr.common.plugin.configureCors
import com.peekr.common.plugin.configureResources
import com.peekr.common.plugin.configureRouting
import com.peekr.common.util.AppDispatchers
import com.peekr.common.util.getTimeZoneInfo
import com.peekr.domain.file.util.fileResourceAutoCleanup
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.netty.EngineMain
import java.util.TimeZone
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    // TODO: 개발 단계에서만 활성화
    intercept(ApplicationCallPipeline.Call) {
        delay(2000L)
        proceed()
    }

    // ------------------------------ Setting ------------------------------
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    // ------------------------------ DI ------------------------------
    configureKoin()

    // ------------------------------ Initialize ------------------------------
    initDatabase()
    runBlocking(AppDispatchers.ioDispatcher) {
        FirebaseAdmin.initialize()
    }

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
    fileResourceAutoCleanup()
}
