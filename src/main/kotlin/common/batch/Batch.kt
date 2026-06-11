package com.turnin.common.batch

import com.turnin.common.util.log.AppLoggerFactory
import io.ktor.server.application.Application
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject

/**
 * 배치 설정
 */
fun Application.configureBatch() {
    val logBackupBatch by inject<LogBackupBatch>()
    val hardDeleteExpiredAccountsBatch by inject<HardDeleteExpiredAccountsBatch>()

    // 로그 백업: KST 01:00
    launch {
        delayUntilNextRun(kstHour = 1, kstMinute = 0)
        while (true) {
            logBackupBatch.run()
            delay(24.hours)
        }
    }

    // 만료 계정 삭제(Hard Delete): KST 02:00
    launch {
        delayUntilNextRun(kstHour = 2, kstMinute = 0)
        while (true) {
            hardDeleteExpiredAccountsBatch.run()
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
    clock: Clock = Clock.system(ZoneId.of("Asia/Seoul")),
    delayFn: suspend (Long) -> Unit = { ms -> delay(ms) },
) {
    val now = LocalDateTime.now(clock)
    val next = now
        .toLocalDate()
        .atTime(kstHour, kstMinute)
        .let { if (it.isBefore(now)) it.plusDays(1) else it }
    val delayMillis = Duration.between(now, next).toMillis()
    LOGGER.info("LogBackupBatch next run at: $next KST")
    delayFn(delayMillis)
}

private val LOGGER = AppLoggerFactory.createLogger("BatchConfig")
