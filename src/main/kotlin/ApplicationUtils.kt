package com.peekr

import com.peekr.common.db.DatabaseFactory
import com.peekr.common.util.TimeZoneInfo
import com.peekr.common.util.config.AppConfig
import com.peekr.common.util.config.RunEnvironment.Companion.toRunEnvironment
import io.ktor.server.application.Application
import kotlin.getValue
import org.koin.ktor.ext.inject

/** Application 유틸 모음 */
object ApplicationUtils {
    /** 데이터베이스 초기화 */
    fun Application.initDatabase() {
        val appConfig by inject<AppConfig>()
        val environment = appConfig.getOrDefault("ktor.environment", "dev").toRunEnvironment()
        val dbUrl = appConfig.getOrDefault("ktor.db.url", "jdbc:postgresql://localhost:5432/defaultdb")
        val dbUser = appConfig.getOrDefault("ktor.db.user", "defaultuser")
        val dbPassword = appConfig.getOrDefault("ktor.db.password", "defaultpassword")
        DatabaseFactory.initialize(environment, dbUrl, dbUser, dbPassword)
    }

    /** 출력해야 하는 경우 깔끔한 출력을 도와준다. */
    inline fun printSection(body: () -> Unit) {
        println("=".repeat(50))
        body()
        println("=".repeat(50))
    }

    /** 서버 세팅 값 출력 */
    fun Application.printServerSettings() {
        val appConfig by inject<AppConfig>()
        val port = appConfig.get("ktor.deployment.port")
        val environment = appConfig.get("ktor.environment")

        println("Server is running in $environment mode")
        println("Listening on port $port")
    }

    fun Application.printTimeZone(timezone: TimeZoneInfo) {
        println("Application TimeZone: ${timezone.appTimeZone}")
        println("Application CurrentTime: ${timezone.appTime}")
        println("DB TimeZone: ${timezone.dbTimeZone}")
        println("DB CurrentTime: ${timezone.dbTime}")

        // App, DB 타임 차이 체크 (1초 이내인지 체크)
        val diffMs = kotlin.math.abs(
            java.time.Duration
                .between(timezone.dbTime, timezone.appTime)
                .toMillis(),
        )
        println(
            "App and DB Time Sync: ${
                if (diffMs > 1000) {
                    "⚠️ App and DB time differ by more than 1 second!"
                } else {
                    "✅"
                }
            }",
        )

        // DB가 UTC인지 체크
        println(
            "DB is Using UTC: ${
                if (timezone.dbTimeZone.equals("UTC", ignoreCase = true)) {
                    "✅"
                } else {
                    "⚠️ DB is using ${timezone.dbTimeZone}, not UTC"
                }
            }",
        )
    }
}
