package com.peekr.common.util.application

import com.peekr.common.di.ApplicationScopeQualifier
import com.peekr.common.util.AppLoggerFactory
import io.ktor.server.application.Application
import kotlin.getValue
import kotlinx.coroutines.CompletableJob
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
 */
fun Application.applicationCleanup(
    cleanups: List<() -> Unit> = emptyList(),
) {
    val applicationScope by inject<CoroutineScope>(ApplicationScopeQualifier)
    val logger = AppLoggerFactory.createLogger("ShutdownHook")

    Runtime.getRuntime().addShutdownHook(
        Thread {
            logger.info("[SHUTDOWN] --- 서버 종료 절차 시작 ---")

            runBlocking {
                // 전체 타임아웃: 참고 수치 (K8s terminationGracePeriodSeconds보다 짧게)
                withTimeoutOrNull(25_000) {
                    // 1. 새 코루틴 생성 차단
                    val parentJob = applicationScope.coroutineContext[Job]
                    if (parentJob is CompletableJob) {
                        parentJob.complete()
                    }

                    // 2. 백그라운드 작업 완료 대기
                    val children = parentJob?.children?.toList() ?: emptyList()
                    logger.info("[SHUTDOWN] 백그라운드 작업 대기: ${children.size}개")
                    children.joinAll()
                    logger.info("[SHUTDOWN] 모든 백그라운드 작업 완료")

                    // 3. 부가 리소스 정리
                    cleanups.forEach { cleanup ->
                        runCatching { cleanup() }
                            .onFailure { logger.error(it, "[SHUTDOWN] 리소스 정리 실패") }
                    }
                    logger.info("[SHUTDOWN] 부가 리소스 정리 완료")

                    // 4. Koin 종료 (onClose 트리거 - DB 등)
                    stopKoin()
                    logger.info("[SHUTDOWN] Koin 종료 완료")

                    logger.info("[SHUTDOWN] --- 모든 종료 절차 완료 ---")
                } ?: run {
                    logger.error("[SHUTDOWN] !!! 타임아웃 - 강제 종료 !!!")
                    // 타임아웃 시에도 Koin은 반드시 정리
                    runCatching { stopKoin() }
                }
            }
        },
    )
}
