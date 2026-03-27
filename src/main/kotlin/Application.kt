package com.peekr

import com.peekr.ApplicationUtils.initDatabase
import com.peekr.ApplicationUtils.printSection
import com.peekr.ApplicationUtils.printServerSettings
import com.peekr.ApplicationUtils.printTimeZone
import com.peekr.common.di.configureKoin
import com.peekr.common.exception.configureExceptionHandler
import com.peekr.common.firebase.FirebaseAdmin
import com.peekr.common.jwt.configureJwtSecurity
import com.peekr.common.plugin.configureAPIDocuments
import com.peekr.common.plugin.configureCallLogging
import com.peekr.common.plugin.configureContentNegotiation
import com.peekr.common.plugin.configureCors
import com.peekr.common.plugin.configureResources
import com.peekr.common.plugin.configureRouting
import com.peekr.common.util.AppDispatchers
import com.peekr.common.util.application.applicationCleanup
import com.peekr.common.util.getTimeZoneInfo
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

    // ------------------------------ Cleanup ------------------------------
    // 프로세스 종료 시 아래 순서로 정리된다:
    // 1. 실행 중인 백그라운드 코루틴 완료 대기
    // 2. Koin onClose 블록 실행 (DB, 외부 서비스 등 리소스 해제)
    applicationCleanup()
}
