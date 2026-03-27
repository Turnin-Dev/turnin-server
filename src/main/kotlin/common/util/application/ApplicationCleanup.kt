package com.peekr.common.util.application

import com.peekr.common.di.ApplicationScopeQualifier
import com.peekr.common.util.AppLoggerFactory
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.getValue
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.context.stopKoin
import org.koin.java.KoinJavaComponent.inject
import org.koin.ktor.ext.inject

/**
 * 애플리케이션 종료 시 리소스 정리 수행 함수
 *
 * @param cleanups 부가 리소스 정리 작업 리스트
 */
fun Application.applicationCleanup(
    cleanups: List<() -> Unit> = emptyList(),
) {
    val cleanupDone = AtomicBoolean(false)

    monitor.subscribe(ApplicationStopped) {
        performCleanup(cleanupDone, cleanups)
    }

    Runtime.getRuntime().addShutdownHook(
        Thread {
            performCleanup(cleanupDone, cleanups)
        },
    )
}

// 종료 작업 수행
private fun Application.performCleanup(
    cleanupDone: AtomicBoolean,
    cleanups: List<() -> Unit>,
) {
    // ------------------------------ 이미 정리 되었는지 체크 ------------------------------
    if (!cleanupDone.compareAndSet(false, true)) {
        LOGGER.info("[SHUTDOWN] 이미 정리 완료, 스킵")
        return
    }

    // ------------------------------ 주입 가능 여부 확인 ------------------------------
    val applicationScope = runCatching {
        inject<CoroutineScope>(ApplicationScopeQualifier).value
    }.getOrNull()

    if (applicationScope == null) {
        LOGGER.error("[SHUTDOWN] ApplicationScope 주입 실패, Koin 정리만 수행")
        runCatching { stopKoin() }
        return
    }

    // ------------------------------ 정리 작업 시작 ------------------------------
    LOGGER.info("[SHUTDOWN] --- 서버 종료 절차 시작 ---")

    runBlocking {
        var parentJobGlobal: Job? = null

        // 전체 타임아웃: 참고 수치 (K8s terminationGracePeriodSeconds보다 짧게)
        withTimeoutOrNull(25_000) {
            // 1. 새 코루틴 생성 차단
            val parentJob = applicationScope.coroutineContext[Job]
            parentJobGlobal = parentJob
            if (parentJob is CompletableJob) {
                parentJob.complete()
            }

            // 2. 백그라운드 작업 완료 대기
            val children = parentJob?.children?.toList() ?: emptyList()
            LOGGER.info("[SHUTDOWN] 백그라운드 작업 대기: ${children.size}개")
            children.joinAll()
            LOGGER.info("[SHUTDOWN] 모든 백그라운드 작업 완료")

            // 3. 부가 리소스 정리
            cleanups.forEach { cleanup ->
                runCatching { cleanup() }
                    .onFailure { LOGGER.error(it, "[SHUTDOWN] 리소스 정리 실패") }
            }
            LOGGER.info("[SHUTDOWN] 부가 리소스 정리 완료")

            // 4. Koin 종료 (onClose 트리거 - DB 등)
            stopKoin()
            LOGGER.info("[SHUTDOWN] Koin 종료 완료")

            LOGGER.info("[SHUTDOWN] --- 모든 종료 절차 완료 ---")
        } ?: run {
            LOGGER.error("[SHUTDOWN] !!! 타임아웃 - 강제 종료 !!!")
            // 타임아웃 시에도 Koin과 코루틴은 반드시 정리
            runCatching { parentJobGlobal?.cancel() }
            runCatching { stopKoin() }
        }
    }
}

val LOGGER = AppLoggerFactory.createLogger("ShutdownHook")
