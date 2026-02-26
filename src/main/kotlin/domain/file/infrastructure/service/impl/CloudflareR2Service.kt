package com.peekr.domain.file.infrastructure.service.impl

import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.config.AppConfig
import com.peekr.common.util.masking
import java.net.URI
import java.time.Duration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
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
) : AutoCloseable {
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

    /**
     * PutObjectRequest 생성
     *
     * 캐시: 1년 설정
     */
    private fun createPutObjectRequest(fileName: String, mimeType: String): PutObjectRequest =
        PutObjectRequest
            .builder()
            .bucket(bucketName)
            .key(fileName)
            .contentType(mimeType)
            .cacheControl("public, max-age=31536000, immutable")
            .build()

    /**
     * S3Presigner 생성
     */
    private fun getS3Presigner(): S3Presigner =
        s3PresignerFactory.createS3Presigner(
            accessKey,
            secretKey,
            region,
            endpoint,
        )

    private val s3ClientDelegate = lazy {
        S3Client
            .builder()
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey),
                ),
            ).region(region)
            .endpointOverride(endpoint)
            .build()
    }

    /** S3Client 생성 (삭제를 위해) */
    private val s3Client: S3Client by s3ClientDelegate

    /**
     * Presigned PUT URL 요청 객체를 생성한다. (업로드 / 업데이트 공용)
     *
     * @param fileName 파일 이름
     * @param mimeType MIME 타입
     */
    fun createPresignedRequest(fileName: String, mimeType: String): PresignedPutObjectRequest {
        try {
            val putObjectRequest = createPutObjectRequest(fileName, mimeType)
            return getS3Presigner().use { presigner ->
                presigner.presignPutObject(
                    PutObjectPresignRequest
                        .builder()
                        .putObjectRequest(putObjectRequest)
                        .signatureDuration(signatureDuration) // 5분 동안 유효한 URL
                        .build(),
                )
            }
        } catch (e: Exception) {
            LOGGER.error(
                e,
                "Failed to create presigned request" +
                    "(bucket=${bucketName.masking()}, key=${fileName.masking()}, contentType=$mimeType)",
            )
            throw e
        }
    }

    /**
     * 서버에서 파일을 직접 삭제한다.
     *
     * @param fileName 파일명
     */
    fun deleteFile(fileName: String) {
        try {
            s3Client.deleteObject(
                DeleteObjectRequest
                    .builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build(),
            )
        } catch (e: Exception) {
            LOGGER.error(
                e,
                "Failed to delete file " +
                    "(bucket=${bucketName.masking()}, key=${fileName.masking()})",
            )
            throw e
        }
    }

    override fun close() {
        if (s3ClientDelegate.isInitialized()) {
            runCatching { s3Client.close() }
                .onFailure { LOGGER.error(it, "Failed to close S3Client") }
        }
    }
}

private val LOGGER = AppLoggerFactory.createLogger("CloudflareR2Service")
