package com.turnin.common.batch

import com.turnin.common.infrastructure.cloudflare.CloudflareR2Client
import com.turnin.common.util.config.AppConfig
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.common.util.log.LogAction
import com.turnin.common.util.log.LogTag
import com.turnin.common.util.log.LogType
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 로그 백업 배치
 *
 * 전날 로그 파일을 R2에 업로드하고 로컬에서 삭제한다.
 *
 * 실패 시 로컬 파일을 삭제하지 않는다. (maxHistory=7 이내 재시도 가능)
 */
class LogBackupBatch(
    private val appConfig: AppConfig,
    private val r2Client: CloudflareR2Client,
    private val normalLogDir: String = "/logs/normal",
    private val privacyLogDir: String = "/logs/privacy",
) {
    private val normalLogBucketName by lazy {
        appConfig.getRequired("ktor.security.cloudflare.s3LogNormalBucketName")
    }
    private val privacyLogBucketName by lazy {
        appConfig.getRequired("ktor.security.cloudflare.s3LogPrivacyBucketName")
    }

    /**
     * 전날 로그 파일을 R2에 백업한다.
     *
     * 일반 로그와 개인정보 로그를 각각 다른 버킷에 업로드하고
     * 업로드 성공한 파일만 로컬에서 삭제한다.
     */
    fun run() {
        val yesterday = LocalDate.now().minusDays(1)
        LOGGER.info(
            "LogBackupBatch running: date=$yesterday",
            mapOf(
                LogTag.LOG_TYPE.key to LogType.NORMAL.value,
                LogTag.ACTION.key to LogAction.LOG_BACKUP_START.value,
            ),
        )

        val normalFailed = backupLog(
            dir = normalLogDir,
            prefix = "app-normal",
            bucketName = normalLogBucketName,
            r2Prefix = "logs/normal",
            date = yesterday,
        )
        val privacyFailed = backupLog(
            dir = privacyLogDir,
            prefix = "app-privacy",
            bucketName = privacyLogBucketName,
            r2Prefix = "logs/privacy",
            date = yesterday,
        )

        if (normalFailed || privacyFailed) {
            LOGGER.warn(
                "LogBackupBatch completed with failures",
                mapOf(
                    LogTag.LOG_TYPE.key to LogType.NORMAL.value,
                    LogTag.ACTION.key to LogAction.LOG_BACKUP_FAILURE.value,
                ),
            )
        } else {
            LOGGER.info(
                "LogBackupBatch completed successfully",
                mapOf(
                    LogTag.LOG_TYPE.key to LogType.NORMAL.value,
                    LogTag.ACTION.key to LogAction.LOG_BACKUP_SUCCESS.value,
                ),
            )
        }
    }

    /**
     * 단일 로그 파일을 R2에 업로드하고 로컬에서 삭제한다.
     *
     * @param dir 로컬 로그 디렉토리
     * @param prefix 로그 파일 접두사
     * @param bucketName 업로드할 R2 버킷명
     * @param r2Prefix R2 저장 경로 접두사
     * @param date 백업할 날짜
     * @return 실패 여부 (true = 실패)
     */
    private fun backupLog(
        dir: String,
        prefix: String,
        bucketName: String,
        r2Prefix: String,
        date: LocalDate,
    ): Boolean {
        val fileName = "$prefix-${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}.log.gz"
        val file = File("$dir/$fileName")

        if (!file.exists()) {
            LOGGER.warn("Log file not found, skipping: ${file.path}")
            return false
        }

        return try {
            r2Client.putObject(
                bucketName = bucketName,
                key = "$r2Prefix/$fileName",
                file = file,
            )
            file.delete()
            LOGGER.info("Log backup success: ${file.path} → r2://$bucketName/$r2Prefix/$fileName")
            false
        } catch (e: Exception) {
            LOGGER.error(e, "Log backup failed: ${file.path}")
            true
        }
    }
}

private val LOGGER = AppLoggerFactory.createLogger("LogBackupBatch")
