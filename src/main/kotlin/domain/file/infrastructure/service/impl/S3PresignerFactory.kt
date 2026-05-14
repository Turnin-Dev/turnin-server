package com.turnin.domain.file.infrastructure.service.impl

import java.net.URI
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.presigner.S3Presigner

class S3PresignerFactory : AutoCloseable {
    @Volatile
    private var s3Presigner: S3Presigner? = null

    /**
     * Presign URL을 생성하기 위한 S3Presigner 객체를 생성합니다.
     *
     * 이 객체는 애플리케이션 생애주기 동안 재사용되는 리소스입니다.
     *
     * @param accessKey R2 Access Key
     * @param secretKey R2 Secret Key
     * @param region R2 Region
     * @param endpoint R2 서비스 엔드포인트
     * @return S3Presigner 인스턴스 (싱글톤)
     */
    fun createS3Presigner(
        accessKey: String,
        secretKey: String,
        region: Region,
        endpoint: URI,
    ): S3Presigner {
        s3Presigner?.let { return it }

        synchronized(this) {
            // Double-checked locking pattern
            s3Presigner?.let { return it }

            s3Presigner = S3Presigner
                .builder()
                .credentialsProvider { AwsBasicCredentials.create(accessKey, secretKey) }
                .region(region)
                .endpointOverride(endpoint)
                .build()
            return s3Presigner!!
        }
    }

    override fun close() {
        synchronized(this) {
            s3Presigner?.close()
            s3Presigner = null
        }
    }
}
