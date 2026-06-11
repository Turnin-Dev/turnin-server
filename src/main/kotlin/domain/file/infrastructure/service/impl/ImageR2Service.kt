package com.turnin.domain.file.infrastructure.service.impl

import com.turnin.common.infrastructure.cloudflare.CloudflareR2Client
import com.turnin.common.util.config.AppConfig
import java.time.Duration
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest

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

    fun createPresignedRequest(fileName: String, mimeType: String): PresignedPutObjectRequest =
        r2Client.createPresignedPutUrl(
            bucketName = bucketName,
            key = fileName,
            mimeType = mimeType,
            duration = signatureDuration,
            cacheControl = "public, max-age=31536000, immutable",
        )

    fun deleteFile(fileName: String) =
        r2Client.deleteObject(
            bucketName = bucketName,
            key = fileName,
        )
}
