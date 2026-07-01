package com.turnin.common.util.application

import com.turnin.common.di.DefaultApplicationScopeQualifier
import com.turnin.common.di.IOApplicationScopeQualifier
import com.turnin.common.util.log.AppLoggerFactory
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.context.stopKoin
import org.koin.ktor.ext.inject

/**
 * 애플리케이션 종료 시 리소스 정리 수행 함수
 *
 * @param cleanups 부가 리소스 정리 작업 리스트
 * @param cancellableJobs 수동 취소할 무한 대기 루프 작업(배치 등) 리스트
 */
fun Application.applicationCleanup(
    cleanups: List<() -> Unit> = emptyList(),
    cancellableJobs: List<Job> = emptyList(),
) {
    val cleanupDone = AtomicBoolean(false)

    val hook = Thread {
        performCleanup(cleanupDone, cleanups, cancellableJobs)
    }
    Runtime.getRuntime().addShutdownHook(hook)

    monitor.subscribe(ApplicationStopped) {
        performCleanup(cleanupDone, cleanups, cancellableJobs)
        runCatching { Runtime.getRuntime().removeShutdownHook(hook) }
    }
}

// 종료 작업 수행
private fun Application.performCleanup(
    cleanupDone: AtomicBoolean,
    cleanups: List<() -> Unit>,
    cancellableJobs: List<Job> = emptyList(),
) {
    // ------------------------------ 이미 정리 되었는지 체크 ------------------------------
    if (!cleanupDone.compareAndSet(false, true)) {
        LOGGER.info("$LOG_NAME 정리 작업이 이미 진행/완료되어 중복 실행 스킵")
        return
    }

    // ------------------------------ 주입 가능 여부 확인 ------------------------------
    val qualifiers = listOf(
        DefaultApplicationScopeQualifier,
        IOApplicationScopeQualifier,
    )
    val applicationScopes = qualifiers.mapNotNull { qualifier ->
        runCatching { inject<CoroutineScope>(qualifier).value }
            .getOrNull()
            .also { if (it == null) LOGGER.error("$LOG_NAME $qualifier 주입 실패") }
    }

    if (applicationScopes.isEmpty()) {
        LOGGER.error("$LOG_NAME 모든 ApplicationScope 주입 실패, Koin 정리만 수행")
        runCatching { stopKoin() }
        return
    }

    if (applicationScopes.size < qualifiers.size) {
        LOGGER.error("$LOG_NAME 일부 ApplicationScope 주입 실패, 해당 scope는 정리되지 않음")
    }

    // ------------------------------ 정리 작업 시작 ------------------------------
    LOGGER.info("$LOG_NAME --- 서버 종료 절차 시작 ---")

    runBlocking {
        val parentJobsGlobal = mutableListOf<Job>()

        // 전체 타임아웃: 참고 수치 (K8s terminationGracePeriodSeconds(30초)보다 짧게)
        withTimeoutOrNull(20_000) {
            // 1. 새 코루틴 생성 차단 + 백그라운드 작업 완료 대기
            applicationScopes.forEach { scope ->
                val parentJob = scope.coroutineContext[Job]
                val parentName = scope.coroutineContext[CoroutineName]?.name ?: "unnamed"
                if (parentJob is CompletableJob) {
                    parentJobsGlobal.add(parentJob)
                    LOGGER.info("$LOG_NAME 백그라운드 작업 대상 ($parentName): $parentJob ")
                    parentJob.complete()
                }
            }

            // 무한 대기 루프 작업(배치 등)만 명시적으로 취소
            if (cancellableJobs.isNotEmpty()) {
                LOGGER.info("$LOG_NAME Jobs 수동 취소 (${cancellableJobs.size}개)")
                cancellableJobs.forEach { it.cancel() }
            }

            // 나머지 작업 종료까지 대기
            parentJobsGlobal.forEach { parentJob ->
                LOGGER.info("$LOG_NAME 백그라운드 작업 대기 ($parentJob)")
                parentJob.join()
                LOGGER.info("$LOG_NAME 백그라운드 작업 정리 완료")
            }

            // 2. 부가 리소스 정리
            cleanups.forEach { cleanup ->
                runCatching { cleanup() }
                    .onFailure { LOGGER.error(it, "$LOG_NAME 리소스 정리 실패") }
            }
            LOGGER.info("$LOG_NAME 부가 리소스 정리 완료")

            // 3. Koin 종료 (onClose 트리거 - DB 등)
            stopKoin()
            LOGGER.info("$LOG_NAME Koin 종료 완료")
            LOGGER.info("$LOG_NAME --- 모든 종료 절차 완료 ---")
        } ?: run {
            LOGGER.error("$LOG_NAME !!! 타임아웃 - 강제 종료 !!!")
            // 타임아웃 시에도 Koin과 코루틴은 반드시 정리
            parentJobsGlobal.forEach { runCatching { it.cancel() } }
            runCatching {
                withTimeoutOrNull(1_000) {
                    parentJobsGlobal.joinAll()
                }
            }

            cleanups.forEach { cleanup ->
                runCatching { cleanup() }
                    .onFailure { LOGGER.error(it, "$LOG_NAME 리소스 정리 실패") }
            }
            runCatching { stopKoin() }
        }
    }
}

private val LOGGER = AppLoggerFactory.createLogger("ShutdownHook")
private const val LOG_NAME = "[SHUTDOWN]"
