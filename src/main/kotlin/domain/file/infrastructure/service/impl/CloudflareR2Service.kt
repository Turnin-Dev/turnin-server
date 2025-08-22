package com.peekr.domain.file.infrastructure.service.impl

import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.config.AppConfig
import com.peekr.common.util.masking
import java.net.URI
import java.time.Duration
import org.koin.java.KoinJavaComponent.inject
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest

/**
 * Cloudflare R2 서비스 클래스
 */
class CloudflareR2Service {
    private val appConfig: AppConfig by inject(AppConfig::class.java)

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
    private val bucketName by lazy {
        appConfig.getOrDefault("ktor.security.cloudflare.s3BucketName", "")
    }
    private val region by lazy {
        Region.of(regionValue)
    }
    private val endpoint by lazy {
        URI.create(endpointValue)
    }

    // Presign URL을 생성하기 위한 객체
    private val s3Presigner by lazy {
        S3Presigner
            .builder()
            .credentialsProvider { AwsBasicCredentials.create(accessKey, secretKey) }
            .region(region)
            .endpointOverride(endpoint)
            .build()
    }

    private fun createPutObjectRequest(fileName: String): PutObjectRequest =
        PutObjectRequest
            .builder()
            .bucket(bucketName)
            .key(fileName)
            .build()

    /**
     * Presigned URL 요청 객체를 생성한다.
     *
     * @param fileName 파일 이름
     */
    fun createPresignedRequest(fileName: String): PresignedPutObjectRequest {
        try {
            val putObjectRequest = createPutObjectRequest(fileName)
            return s3Presigner.presignPutObject(
                PutObjectPresignRequest
                    .builder()
                    .putObjectRequest(putObjectRequest)
                    .signatureDuration(Duration.ofMinutes(10)) // 10분 동안 유효한 URL
                    .build(),
            )
        } catch (e: Exception) {
            LOGGER.debug("Can't create presigned request(bucketName: ${bucketName.masking()}): ${e.message}\n")
            throw e
        }
    }
}

private val LOGGER = AppLoggerFactory.createLogger("CloudflareR2Service")
