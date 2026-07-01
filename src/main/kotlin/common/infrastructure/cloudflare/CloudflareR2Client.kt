package com.turnin.common.infrastructure.cloudflare

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.deleteObject
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.services.s3.presigners.presignPutObject
import aws.sdk.kotlin.services.s3.putObject
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.collections.Attributes
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.fromFile
import aws.smithy.kotlin.runtime.net.url.Url
import com.turnin.common.util.config.AppConfig
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.common.util.masking
import java.io.File
import java.time.Duration
import kotlin.time.toKotlinDuration

/**
 * Cloudflare R2 저수준 클라이언트 (Kotlin SDK 버전)
 *
 * 버킷을 파라미터로 받아 범용적으로 사용 가능하다.
 */
class CloudflareR2Client(private val appConfig: AppConfig) : AutoCloseable {
    private val accessKey by lazy {
        appConfig.getRequired("ktor.security.cloudflare.s3AccessKey")
    }
    private val secretKey by lazy {
        appConfig.getRequired("ktor.security.cloudflare.s3SecretKey")
    }
    private val regionName by lazy {
        appConfig.getRequired("ktor.security.cloudflare.s3Region")
    }
    private val endpoint by lazy {
        appConfig.getRequired("ktor.security.cloudflare.s3Endpoint")
    }

    // 자격 증명 프로바이더 구성
    private val r2CredentialProvider = object : CredentialsProvider {
        override suspend fun resolve(attributes: Attributes): Credentials =
            Credentials(
                accessKeyId = accessKey,
                secretAccessKey = secretKey,
            )
    }

    // 코틀린 S3Client는 단 하나로 일반 요청과 Presign을 모두 처리한다.
    private val s3ClientDelegate = lazy {
        S3Client {
            region = regionName
            endpointUrl = Url.parse(endpoint)
            credentialsProvider = r2CredentialProvider
        }
    }
    private val s3Client: S3Client by s3ClientDelegate

    /**
     * 파일을 R2에 직접 업로드한다.
     *
     * 로그 파일 등 서버에서 직접 R2로 업로드할 때 사용한다.
     * Presigned URL 방식과 달리 서버가 직접 업로드를 처리한다.
     *
     * @param bucketName 대상 버킷명
     * @param key 저장 경로 (파일명 포함, 예: logs/normal/app-normal-2026-01-01.log)
     * @param file 업로드할 파일
     * @param contentType MIME 타입 (기본값: text/plain)
     * @param cacheControl 캐시 설정 (null이면 설정 안 함, 이미지처럼 장기 캐시가 필요한 경우에만 사용)
     */
    suspend fun putObject(
        bucketName: String,
        key: String,
        file: File,
        contentType: String = "text/plain",
        cacheControl: String? = null,
    ) {
        try {
            s3Client.putObject {
                this.bucket = bucketName
                this.key = key
                this.contentType = contentType
                this.cacheControl = cacheControl
                this.body = ByteStream.fromFile(file)
            }
        } catch (e: Exception) {
            LOGGER.error(e, "Failed to put object (bucket=${bucketName.masking()}, key=${key.masking()})")
            throw e
        }
    }

    /**
     * R2에서 파일을 삭제한다.
     *
     * @param bucketName 대상 버킷명
     * @param key 삭제할 파일 경로
     */
    suspend fun deleteObject(bucketName: String, key: String) {
        try {
            s3Client.deleteObject {
                this.bucket = bucketName
                this.key = key
            }
        } catch (e: Exception) {
            LOGGER.error(e, "Failed to delete object (bucket=${bucketName.masking()}, key=${key.masking()})")
            throw e
        }
    }

    /**
     * Presigned PUT URL을 생성한다.
     *
     * 클라이언트가 서버를 거치지 않고 R2에 직접 업로드할 수 있도록
     * 서명된 임시 URL을 발급한다. 이미지 업로드 등 클라이언트 직접 업로드에 사용한다.
     *
     * @param bucketName 대상 버킷명
     * @param key 저장 경로 (파일명 포함)
     * @param mimeType MIME 타입
     * @param duration URL 유효 기간 (기본값: 5분)
     * @param cacheControl 캐시 설정 (null이면 설정 안 함, 이미지처럼 장기 캐시가 필요한 경우에만 사용)
     *
     * @return [String] 타입의 PresignedUrl
     */
    suspend fun createPresignedPutUrl(
        bucketName: String,
        key: String,
        mimeType: String,
        duration: Duration = Duration.ofMinutes(5),
        cacheControl: String? = null,
    ): String {
        try {
            val unsignedRequest = PutObjectRequest {
                this.bucket = bucketName
                this.key = key
                this.contentType = mimeType
                this.cacheControl = cacheControl
            }

            val presignedHttpRequest = s3Client.presignPutObject(unsignedRequest, duration.toKotlinDuration())

            return presignedHttpRequest.url.toString()
        } catch (e: Exception) {
            LOGGER.error(e, "Failed to create presigned URL (bucket=${bucketName.masking()}, key=${key.masking()})")
            throw e
        }
    }

    /**
     * 클라이언트를 닫고 리소스를 해제한다.
     *
     * S3Client와 S3Presigner는 생성 비용이 크므로 앱 종료 시점에만 닫는다.
     * lazy 초기화된 경우에만 close를 시도한다.
     */
    override fun close() {
        if (s3ClientDelegate.isInitialized()) {
            runCatching { s3Client.close() }
                .onFailure { LOGGER.error(it, "Failed to close S3Client") }
        }
    }
}

private val LOGGER = AppLoggerFactory.createLogger("CloudflareR2Client")
