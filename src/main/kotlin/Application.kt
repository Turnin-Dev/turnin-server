package com.turnin

import com.turnin.ApplicationUtils.initDatabase
import com.turnin.ApplicationUtils.printSection
import com.turnin.ApplicationUtils.printServerSettings
import com.turnin.ApplicationUtils.printTimeZone
import com.turnin.common.batch.configureBatch
import com.turnin.common.di.configureKoin
import com.turnin.common.exception.configureExceptionHandler
import com.turnin.common.firebase.FirebaseAdmin
import com.turnin.common.jwt.configureJwtSecurity
import com.turnin.common.plugin.configureAPIDocuments
import com.turnin.common.plugin.configureCallLogging
import com.turnin.common.plugin.configureContentNegotiation
import com.turnin.common.plugin.configureCors
import com.turnin.common.plugin.configureResources
import com.turnin.common.plugin.configureRouting
import com.turnin.common.util.application.applicationCleanup
import com.turnin.common.util.config.AppConfig
import com.turnin.common.util.getTimeZoneInfo
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain
import java.util.TimeZone
import org.koin.ktor.ext.inject

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    // ------------------------------ Setting ------------------------------
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    // ------------------------------ DI ------------------------------
    configureKoin()

    // ------------------------------ Initialize ------------------------------
    val appConfig: AppConfig by inject()
    initDatabase()
    FirebaseAdmin.initialize(appConfig)

    // ------------------------------ Plugins ------------------------------
    configureResources()
    configureCors()

    configureContentNegotiation()
    configureExceptionHandler()
    configureCallLogging()

    configureJwtSecurity()

    configureAPIDocuments()
    configureRouting()

    // ------------------------------ Batch ------------------------------
    val batchJobs = configureBatch()

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
    applicationCleanup(
        cancellableJobs = batchJobs,
    )
}
