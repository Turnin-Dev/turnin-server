package com.turnin.domain.file.infrastructure.service.impl

import com.turnin.common.infrastructure.cloudflare.CloudflareR2Client
import com.turnin.common.util.config.AppConfig
import java.time.Duration

/**
 * 이미지 파일 전용 R2 서비스
 */
class ImageR2Service(
    private val appConfig: AppConfig,
    private val r2Client: CloudflareR2Client,
) {
    private val bucketName by lazy {
        appConfig.getRequired("ktor.security.cloudflare.s3BucketName")
    }

    val signatureDuration: Duration = Duration.ofMinutes(5)

    /**
     * Presigned URL을 생성한다.
     *
     * @param fileName 업로드할 파일명
     * @param mimeType MIME 타입
     *
     * @return [String] 타입의 PresignedUrl
     */
    suspend fun createPresignedUrl(
        fileName: String,
        mimeType: String,
    ): String =
        r2Client.createPresignedPutUrl(
            bucketName = bucketName,
            key = fileName,
            mimeType = mimeType,
            duration = signatureDuration,
            cacheControl = "public, max-age=31536000, immutable",
        )

    /**
     * R2에서 파일을 삭제한다.
     *
     * @param fileName 삭제할 파일명
     */
    suspend fun deleteFile(fileName: String) =
        r2Client.deleteObject(
            bucketName = bucketName,
            key = fileName,
        )
}
