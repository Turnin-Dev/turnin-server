package com.turnin.common.batch

import com.turnin.common.infrastructure.cloudflare.CloudflareR2Client
import com.turnin.common.util.TurninDateTime
import com.turnin.common.util.config.AppConfig
import com.turnin.common.util.toKstDate
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import java.io.File
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
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
    private val today = TurninDateTime.now().toKstDate()
    private val yesterday = today.minusDays(1)
    private val dateStr = yesterday.format(DateTimeFormatter.ISO_LOCAL_DATE)
    private val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)

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
            coroutineDispatcher = UnconfinedTestDispatcher(),
        )
    }

    // ================ run() - 성공 케이스 ================

    @Test
    fun `run - 정상 및 개인정보 로그 모두 업로드 성공 시 두 파일 모두 삭제된다`() = runTest {
        val normalFile = createTempLogFile(normalLogDir, "app-normal")
        val privacyFile = createTempLogFile(privacyLogDir, "app-privacy")

        coEvery { r2Client.putObject(any(), any(), any(), any()) } just Runs

        batch.run()

        coVerify(exactly = 1) {
            r2Client.putObject(
                normalBucket,
                "logs/normal/app-normal-$dateStr.log.gz",
                normalFile,
                "application/gzip",
            )
        }
        coVerify(exactly = 1) {
            r2Client.putObject(
                privacyBucket,
                "logs/privacy/app-privacy-$dateStr.log.gz",
                privacyFile,
                "application/gzip",
            )
        }
        assertTrue(!normalFile.exists()) { "normalFile should be deleted" }
        assertTrue(!privacyFile.exists()) { "privacyFile should be deleted" }
    }

    // ================ run() - 실패 케이스 ================

    @Test
    fun `run - 정상 로그 업로드 실패 시 해당 파일이 삭제되지 않는다`() = runTest {
        val normalFile = createTempLogFile(normalLogDir, "app-normal")
        val privacyFile = createTempLogFile(privacyLogDir, "app-privacy")

        coEvery { r2Client.putObject(normalBucket, any(), any(), any()) } throws RuntimeException("R2 upload error")
        coEvery { r2Client.putObject(privacyBucket, any(), any(), any()) } just Runs

        batch.run()

        assertTrue(normalFile.exists()) { "normalFile should NOT be deleted on failure" }
        assertTrue(!privacyFile.exists()) { "privacyFile should be deleted on success" }
    }

    @Test
    fun `run - 개인정보 로그 업로드 실패 시 해당 파일이 삭제되지 않는다`() = runTest {
        val normalFile = createTempLogFile(normalLogDir, "app-normal")
        val privacyFile = createTempLogFile(privacyLogDir, "app-privacy")

        coEvery { r2Client.putObject(normalBucket, any(), any(), any()) } just Runs
        coEvery { r2Client.putObject(privacyBucket, any(), any(), any()) } throws RuntimeException("R2 upload error")

        batch.run()

        assertTrue(!normalFile.exists()) { "normalFile should be deleted on success" }
        assertTrue(privacyFile.exists()) { "privacyFile should NOT be deleted on failure" }
    }

    @Test
    fun `run - 두 로그 모두 업로드 실패 시 두 파일 모두 삭제되지 않는다`() = runTest {
        val normalFile = createTempLogFile(normalLogDir, "app-normal")
        val privacyFile = createTempLogFile(privacyLogDir, "app-privacy")

        coEvery {
            r2Client.putObject(any(), any(), any(), any())
        } throws RuntimeException("R2 upload error")

        batch.run()

        assertTrue(normalFile.exists()) { "normalFile should NOT be deleted on failure" }
        assertTrue(privacyFile.exists()) { "privacyFile should NOT be deleted on failure" }
    }

    // ================ run() - 파일 없음 ================

    @Test
    fun `run - 로그 파일이 존재하지 않으면 R2 업로드를 호출하지 않는다`() = runTest {
        batch.run()

        coVerify(exactly = 0) { r2Client.putObject(any(), any(), any(), any()) }
    }

    @Test
    fun `run - 정상 로그 파일만 없는 경우 개인정보 로그만 업로드한다`() = runTest {
        val privacyFile = createTempLogFile(privacyLogDir, "app-privacy")

        coEvery { r2Client.putObject(privacyBucket, any(), any(), any()) } just Runs

        batch.run()

        coVerify(exactly = 0) { r2Client.putObject(normalBucket, any(), any(), any()) }
        coVerify(exactly = 1) { r2Client.putObject(privacyBucket, any(), privacyFile, any()) }
        assertTrue(!privacyFile.exists())
    }

    @Test
    fun `run - 개인정보 로그 파일만 없는 경우 정상 로그만 업로드한다`() = runTest {
        val normalFile = createTempLogFile(normalLogDir, "app-normal")

        coEvery { r2Client.putObject(normalBucket, any(), any(), any()) } just Runs

        batch.run()

        coVerify(exactly = 1) { r2Client.putObject(normalBucket, any(), normalFile, any()) }
        coVerify(exactly = 0) { r2Client.putObject(privacyBucket, any(), any(), any()) }
        assertTrue(!normalFile.exists())
    }

    // ================ R2 key 경로 검증 ================

    @Test
    fun `backupLog - R2 key가 올바른 prefix와 파일명으로 구성된다`() = runTest {
        createTempLogFile(normalLogDir, "app-normal")

        val keySlot = slot<String>()
        coEvery { r2Client.putObject(any(), capture(keySlot), any(), any()) } just Runs

        batch.run()

        assertEquals("logs/normal/app-normal-$dateStr.log.gz", keySlot.captured)
    }

    // ================ 오늘 날짜 파일 제외 ================

    @Test
    fun `run - 오늘 날짜 로그 파일은 업로드하지 않는다`() = runTest {
        createTempLogFileWithDate(normalLogDir, "app-normal", todayStr)

        batch.run()

        coVerify(exactly = 0) { r2Client.putObject(any(), any(), any(), any()) }
    }

    @Test
    fun `run - 오늘 날짜 파일과 이전 날짜 파일이 혼재할 때 이전 날짜 파일만 업로드한다`() = runTest {
        val yesterdayFile = createTempLogFile(normalLogDir, "app-normal")
        val todayFile = createTempLogFileWithDate(normalLogDir, "app-normal", todayStr)

        coEvery { r2Client.putObject(any(), any(), any(), any()) } just Runs

        batch.run()

        coVerify(exactly = 1) { r2Client.putObject(any(), any(), yesterdayFile, any()) }
        coVerify(exactly = 0) { r2Client.putObject(any(), any(), todayFile, any()) }
        assertTrue(!yesterdayFile.exists()) { "yesterdayFile should be deleted" }
        assertTrue(todayFile.exists()) { "todayFile should NOT be deleted" }
    }

    // ================ 미백업 파일 재시도 ================

    @Test
    fun `run - 이전에 실패한 파일이 남아있으면 재시도한다`() = runTest {
        val twoDaysAgoStr = today.minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val oldFile = createTempLogFileWithDate(normalLogDir, "app-normal", twoDaysAgoStr)
        val yesterdayFile = createTempLogFile(normalLogDir, "app-normal")

        coEvery { r2Client.putObject(any(), any(), any(), any()) } just Runs

        batch.run()

        coVerify(exactly = 1) { r2Client.putObject(any(), any(), oldFile, any()) }
        coVerify(exactly = 1) { r2Client.putObject(any(), any(), yesterdayFile, any()) }
        assertTrue(!oldFile.exists()) { "oldFile should be deleted" }
        assertTrue(!yesterdayFile.exists()) { "yesterdayFile should be deleted" }
    }

    @Test
    fun `run - 이전에 실패한 파일 재시도 중 실패하면 파일이 삭제되지 않는다`() = runTest {
        val twoDaysAgoStr = today.minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val oldFile = createTempLogFileWithDate(normalLogDir, "app-normal", twoDaysAgoStr)

        coEvery { r2Client.putObject(any(), any(), any(), any()) } throws RuntimeException("R2 upload error")

        batch.run()

        assertTrue(oldFile.exists()) { "oldFile should NOT be deleted on failure" }
    }

    // ================ 헬퍼 ================

    private fun createTempLogFile(dir: File, prefix: String): File =
        createTempLogFileWithDate(dir, prefix, dateStr)

    private fun createTempLogFileWithDate(dir: File, prefix: String, date: String): File =
        File(dir, "$prefix-$date.log.gz").apply {
            writeText("dummy log content")
        }
}
