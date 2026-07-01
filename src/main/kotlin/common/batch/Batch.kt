package com.turnin.common.batch

import com.turnin.common.di.DefaultApplicationScopeQualifier
import com.turnin.common.util.log.AppLoggerFactory
import io.ktor.server.application.Application
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject

// TODO: 배치 등록은 Default 에서 실제 작업(IO 작업?)은 IO 에서 실행해야 하지 않나?

/**
 * 배치 설정
 */
fun Application.configureBatch() {
    val applicationScope by inject<CoroutineScope>(DefaultApplicationScopeQualifier)
    val logBackupBatch by inject<LogBackupBatch>()
    val hardDeleteExpiredAccountsBatch by inject<HardDeleteExpiredAccountsBatch>()

    // 로그 백업: KST 01:00
    applicationScope.launch {
        val logBackUpBatchName = "LogBackupBatch"
        delayUntilNextRun(kstHour = 1, kstMinute = 0, batchName = logBackUpBatchName)
        while (true) {
            batchTryCatch(logBackUpBatchName) {
                logBackupBatch.run()
            }
            delay(24.hours)
        }
    }

    // 만료 계정 삭제(Hard Delete): KST 02:00
    applicationScope.launch {
        val accountDeletionBatchName = "AccountDeletionBatch"
        delayUntilNextRun(kstHour = 2, kstMinute = 0, batchName = accountDeletionBatchName)
        while (true) {
            batchTryCatch(accountDeletionBatchName) {
                hardDeleteExpiredAccountsBatch.run()
            }
            delay(24.hours)
        }
    }

    // 다른 배치 추가 시 예시
//    // 계정 삭제: KST 01:10
//    launch {
//        delayUntilNextRun(kstHour = 1, kstMinute = 10)
//        while (true) {
//            accountDeletionBatch.run()
//            delay(24.hours)
//        }
//    }
}

/**
 * 다음 특정 시각(KST)까지 대기한다.
 *
 * 이미 지난 시각이면 다음 날 해당 시각까지 대기한다.
 *
 * @param kstHour 실행할 시각 KST 기준 시 (0~23)
 * @param kstMinute 실행할 시각 KST 기준 분 (0~59)
 * @param clock 현재 시각 제공자 (테스트 시 고정 시각 주입 가능)
 * @param delayFn 대기 함수 (테스트 시 실제 대기 없이 교체 가능)
 */
internal suspend fun delayUntilNextRun(
    kstHour: Int,
    kstMinute: Int = 0,
    batchName: String,
    clock: Clock = Clock.system(ZoneId.of("Asia/Seoul")),
    delayFn: suspend (Long) -> Unit = { ms -> delay(ms) },
) {
    val now = LocalDateTime.now(clock)
    val next = now
        .toLocalDate()
        .atTime(kstHour, kstMinute)
        .let { if (it.isBefore(now)) it.plusDays(1) else it }
    val delayMillis = Duration.between(now, next).toMillis()
    LOGGER.info("$batchName next run at: $next KST")
    delayFn(delayMillis)
}

/**
 * 배치 전용 try-catch
 *
 * @param batchName 배치명
 * @param block 배치 수행 블록
 */
private suspend inline fun batchTryCatch(
    batchName: String,
    block: suspend () -> Unit,
) {
    try {
        block()
    } catch (e: Exception) {
        LOGGER.error(e, "Batch '$batchName' failed")
    }
}

private val LOGGER = AppLoggerFactory.createLogger("BatchConfig")
