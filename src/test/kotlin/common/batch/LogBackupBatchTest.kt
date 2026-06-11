package com.turnin.common.batch

import com.turnin.common.infrastructure.cloudflare.CloudflareR2Client
import com.turnin.common.util.config.AppConfig
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class LogBackupBatchTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var appConfig: AppConfig
    private lateinit var r2Client: CloudflareR2Client
    private lateinit var batch: LogBackupBatch

    private lateinit var normalLogDir: File
    private lateinit var privacyLogDir: File

    private val normalBucket = "normal-bucket"
    private val privacyBucket = "privacy-bucket"
    private val yesterday = LocalDate.now().minusDays(1)
    private val dateStr = yesterday.format(DateTimeFormatter.ISO_LOCAL_DATE)

    @Before
    fun setUp() {
        appConfig = mockk()
        r2Client = mockk()

        normalLogDir = tempFolder.newFolder("normal")
        privacyLogDir = tempFolder.newFolder("privacy")

        every {
            appConfig.getRequired("ktor.security.cloudflare.s3LogNormalBucketName")
        } returns normalBucket
        every {
            appConfig.getRequired("ktor.security.cloudflare.s3LogPrivacyBucketName")
        } returns privacyBucket

        batch = LogBackupBatch(
            appConfig = appConfig,
            r2Client = r2Client,
            normalLogDir = normalLogDir.absolutePath,
            privacyLogDir = privacyLogDir.absolutePath,
        )
    }

    // ================ run() - 성공 케이스 ================

    @Test
    fun `run - 정상 및 개인정보 로그 모두 업로드 성공 시 두 파일 모두 삭제된다`() {
        val normalFile = createTempLogFile(normalLogDir, "app-normal")
        val privacyFile = createTempLogFile(privacyLogDir, "app-privacy")

        every { r2Client.putObject(any(), any(), any()) } just Runs

        batch.run()

        verify(exactly = 1) {
            r2Client.putObject(normalBucket, "logs/normal/app-normal-$dateStr.log.gz", normalFile)
        }
        verify(exactly = 1) {
            r2Client.putObject(privacyBucket, "logs/privacy/app-privacy-$dateStr.log.gz", privacyFile)
        }
        assert(!normalFile.exists()) { "normalFile should be deleted" }
        assert(!privacyFile.exists()) { "privacyFile should be deleted" }
    }

    // ================ run() - 실패 케이스 ================

    @Test
    fun `run - 정상 로그 업로드 실패 시 해당 파일이 삭제되지 않는다`() {
        val normalFile = createTempLogFile(normalLogDir, "app-normal")
        val privacyFile = createTempLogFile(privacyLogDir, "app-privacy")

        every { r2Client.putObject(normalBucket, any(), any()) } throws RuntimeException("R2 upload error")
        every { r2Client.putObject(privacyBucket, any(), any()) } just Runs

        batch.run()

        assert(normalFile.exists()) { "normalFile should NOT be deleted on failure" }
        assert(!privacyFile.exists()) { "privacyFile should be deleted on success" }
    }

    @Test
    fun `run - 개인정보 로그 업로드 실패 시 해당 파일이 삭제되지 않는다`() {
        val normalFile = createTempLogFile(normalLogDir, "app-normal")
        val privacyFile = createTempLogFile(privacyLogDir, "app-privacy")

        every { r2Client.putObject(normalBucket, any(), any()) } just Runs
        every { r2Client.putObject(privacyBucket, any(), any()) } throws RuntimeException("R2 upload error")

        batch.run()

        assert(!normalFile.exists()) { "normalFile should be deleted on success" }
        assert(privacyFile.exists()) { "privacyFile should NOT be deleted on failure" }
    }

    @Test
    fun `run - 두 로그 모두 업로드 실패 시 두 파일 모두 삭제되지 않는다`() {
        val normalFile = createTempLogFile(normalLogDir, "app-normal")
        val privacyFile = createTempLogFile(privacyLogDir, "app-privacy")

        every { r2Client.putObject(any(), any(), any()) } throws RuntimeException("R2 upload error")

        batch.run()

        assert(normalFile.exists()) { "normalFile should NOT be deleted on failure" }
        assert(privacyFile.exists()) { "privacyFile should NOT be deleted on failure" }
    }

    // ================ run() - 파일 없음 ================

    @Test
    fun `run - 로그 파일이 존재하지 않으면 R2 업로드를 호출하지 않는다`() {
        batch.run()

        verify(exactly = 0) { r2Client.putObject(any(), any(), any()) }
    }

    @Test
    fun `run - 정상 로그 파일만 없는 경우 개인정보 로그만 업로드한다`() {
        val privacyFile = createTempLogFile(privacyLogDir, "app-privacy")

        every { r2Client.putObject(privacyBucket, any(), any()) } just Runs

        batch.run()

        verify(exactly = 0) { r2Client.putObject(normalBucket, any(), any()) }
        verify(exactly = 1) { r2Client.putObject(privacyBucket, any(), privacyFile) }
        assert(!privacyFile.exists())
    }

    @Test
    fun `run - 개인정보 로그 파일만 없는 경우 정상 로그만 업로드한다`() {
        val normalFile = createTempLogFile(normalLogDir, "app-normal")

        every { r2Client.putObject(normalBucket, any(), any()) } just Runs

        batch.run()

        verify(exactly = 1) { r2Client.putObject(normalBucket, any(), normalFile) }
        verify(exactly = 0) { r2Client.putObject(privacyBucket, any(), any()) }
        assert(!normalFile.exists())
    }

    // ================ R2 key 경로 검증 ================

    @Test
    fun `backupLog - R2 key가 올바른 prefix와 파일명으로 구성된다`() {
        createTempLogFile(normalLogDir, "app-normal")

        val keySlot = slot<String>()
        every { r2Client.putObject(any(), capture(keySlot), any()) } just Runs

        batch.run()

        assertEquals("logs/normal/app-normal-$dateStr.log.gz", keySlot.captured)
    }

    // ================ 헬퍼 ================

    private fun createTempLogFile(dir: File, prefix: String): File =
        File(dir, "$prefix-$dateStr.log.gz").apply {
            writeText("dummy log content")
        }
}
