package com.peekr.domain.file.infrastructure.service.impl

import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.config.AppConfig
import com.peekr.common.util.masking
import java.net.URI
import java.time.Duration
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest

/**
 * Cloudflare R2 서비스 클래스
 */
class CloudflareR2Service(
    private val appConfig: AppConfig,
    private val s3PresignerFactory: S3PresignerFactory,
) {
    private val accessKey by lazy {
        appConfig.getOrDefault("ktor.security.cloudflare.s3AccessKey", "")
    }
    private val secretKey by lazy {
        appConfig.getOrDefault("ktor.security.cloudflare.s3SecretKey", "")
    }
    private val regionValue by lazy {
        appConfig.getOrDefault("ktor.security.cloudflare.s3Region", "")
    }
    private val endpointValue by lazy {
        appConfig.getOrDefault("ktor.security.cloudflare.s3Endpoint", "")
    }
    private val region by lazy {
        Region.of(regionValue)
    }
    private val endpoint by lazy {
        URI.create(endpointValue)
    }
    private val bucketName by lazy {
        appConfig.getOrDefault("ktor.security.cloudflare.s3BucketName", "")
    }

    // 5분
    val signatureDuration: Duration = Duration.ofMinutes(5)

    private fun createPutObjectRequest(fileName: String, mimeType: String): PutObjectRequest =
        PutObjectRequest
            .builder()
            .bucket(bucketName)
            .key(fileName)
            .contentType(mimeType)
            .build()

    private fun getS3Presigner(): S3Presigner =
        s3PresignerFactory.createS3Presigner(
            accessKey,
            secretKey,
            region,
            endpoint,
        )

    /**
     * Presigned URL 요청 객체를 생성한다.
     *
     * @param fileName 파일 이름
     */
    fun createPresignedRequest(fileName: String, mimeType: String): PresignedPutObjectRequest {
        try {
            val putObjectRequest = createPutObjectRequest(fileName, mimeType)
            val s3Presigner = getS3Presigner()
            try {
                return s3Presigner.presignPutObject(
                    PutObjectPresignRequest
                        .builder()
                        .putObjectRequest(putObjectRequest)
                        .signatureDuration(signatureDuration) // 5분 동안 유효한 URL
                        .build(),
                )
            } finally {
                s3Presigner.close()
            }
        } catch (e: Exception) {
            LOGGER.error(
                e,
                "Failed to create presigned request(bucket=${bucketName.masking()}, key=${fileName.masking()})",
            )
            throw e
        }
    }
}

private val LOGGER = AppLoggerFactory.createLogger("CloudflareR2Service")
