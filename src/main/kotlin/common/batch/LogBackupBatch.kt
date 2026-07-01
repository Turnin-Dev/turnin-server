package com.turnin.common.batch

import com.turnin.common.infrastructure.cloudflare.CloudflareR2Client
import com.turnin.common.util.TurninDateTime
import com.turnin.common.util.config.AppConfig
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.common.util.log.LogAction
import com.turnin.common.util.log.LogTag
import com.turnin.common.util.log.LogType
import com.turnin.common.util.toKstDate
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * 로그 백업 배치
 *
 * 로컬에 남아있는 미백업 로그 파일을 R2에 업로드하고 로컬에서 삭제한다.
 * 이전에 실패한 파일도 재시도한다. (오늘 날짜 파일 제외)
 *
 * 실패 시 로컬 파일을 삭제하지 않는다. (maxHistory=7 이내 재시도 가능)
 */
class LogBackupBatch(
    private val appConfig: AppConfig,
    private val r2Client: CloudflareR2Client,
    private val normalLogDir: String = "/logs/normal",
    private val privacyLogDir: String = "/logs/privacy",
    private val coroutineDispatcher: CoroutineDispatcher,
) {
    private val normalLogBucketName by lazy {
        appConfig.getRequired("ktor.security.cloudflare.s3LogNormalBucketName")
    }
    private val privacyLogBucketName by lazy {
        appConfig.getRequired("ktor.security.cloudflare.s3LogPrivacyBucketName")
    }

    /**
     * 로컬에 남아있는 미백업 로그 파일을 R2에 백업한다.
     *
     * 일반 로그와 개인정보 로그를 각각 다른 버킷에 업로드하고
     * 업로드 성공한 파일만 로컬에서 삭제한다.
     * 이전에 실패한 파일도 재시도한다. (오늘 날짜 파일 제외)
     *
     * **내부 작동:** 파일 작업은 IO 스레드풀에서 스레드를 점유한 채 수행되고,
     *  네트워크 작업도 IO 스레드풀에 실행되지만 내부적으로 스레드 점유/반납으로 더 효율적으로 수행된다.
     */
    suspend fun run() {
        val today = TurninDateTime.now().toKstDate()
        LOGGER.info(
            "LogBackupBatch running: date=$today",
            mapOf(
                LogTag.LOG_TYPE.key to LogType.NORMAL.value,
                LogTag.ACTION.key to LogAction.LOG_BACKUP_BATCH_START.value,
            ),
        )

        val normalSuccess = backupLogs(
            dir = normalLogDir,
            prefix = "app-normal",
            bucketName = normalLogBucketName,
            r2Prefix = "logs/normal",
            today = today,
        )
        val privacySuccess = backupLogs(
            dir = privacyLogDir,
            prefix = "app-privacy",
            bucketName = privacyLogBucketName,
            r2Prefix = "logs/privacy",
            today = today,
        )

        if (normalSuccess && privacySuccess) {
            LOGGER.info(
                "LogBackupBatch completed successfully",
                mapOf(
                    LogTag.LOG_TYPE.key to LogType.NORMAL.value,
                    LogTag.ACTION.key to LogAction.LOG_BACKUP_BATCH_SUCCESS.value,
                ),
            )
        } else {
            LOGGER.warn(
                "LogBackupBatch completed with failures",
                mapOf(
                    LogTag.LOG_TYPE.key to LogType.NORMAL.value,
                    LogTag.ACTION.key to LogAction.LOG_BACKUP_BATCH_FAILURE.value,
                ),
            )
        }
    }

    /**
     * 디렉토리 내 미백업 로그 파일을 전부 R2에 업로드한다.
     *
     * 오늘 날짜 파일은 아직 쓰는 중이므로 제외한다.
     *
     * @param dir 로컬 로그 디렉토리
     * @param prefix 로그 파일 접두사
     * @param bucketName 업로드할 R2 버킷명
     * @param r2Prefix R2 저장 경로 접두사
     * @param today 오늘 날짜 (제외 기준)
     * @return 성공 여부 (true = 성공)
     */
    private suspend fun backupLogs(
        dir: String,
        prefix: String,
        bucketName: String,
        r2Prefix: String,
        today: LocalDate,
    ): Boolean = withContext(coroutineDispatcher) {
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val logDir = File(dir)
        if (!logDir.exists() || !logDir.isDirectory) {
            LOGGER.error("Invalid log backup directory: $dir")
            return@withContext false
        }

        val files = logDir.listFiles { f ->
            f.name.startsWith(prefix) &&
                f.name.endsWith(".log.gz") &&
                !f.name.contains(todayStr)
        } ?: run {
            LOGGER.error("Failed to list log files in $dir")
            return@withContext false
        }

        if (files.isEmpty()) {
            LOGGER.info("No log files to backup in $dir")
            return@withContext true
        }

        var success = true
        files.forEach { file ->
            success = backupLog(file, bucketName, r2Prefix) && success
        }
        return@withContext success
    }

    /**
     * 단일 로그 파일을 R2에 업로드하고 로컬에서 삭제한다.
     *
     * @param file 업로드할 로그 파일
     * @param bucketName 업로드할 R2 버킷명
     * @param r2Prefix R2 저장 경로 접두사
     * @return 성공 여부 (true = 성공)
     */
    private suspend fun backupLog(
        file: File,
        bucketName: String,
        r2Prefix: String,
    ): Boolean = try {
        r2Client.putObject(
            bucketName = bucketName,
            key = "$r2Prefix/${file.name}",
            file = file,
            contentType = "application/gzip",
        )
        val deleted = file.delete()
        if (deleted) {
            LOGGER.info("Log backup success: ${file.path} → r2://$bucketName/$r2Prefix/${file.name}")
            true
        } else {
            LOGGER.error("Failed to delete local log file after upload: ${file.path}")
            false
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        LOGGER.error(e, "Log backup failed: ${file.path}")
        false
    }
}

private val LOGGER = AppLoggerFactory.createLogger("LogBackupBatch")
