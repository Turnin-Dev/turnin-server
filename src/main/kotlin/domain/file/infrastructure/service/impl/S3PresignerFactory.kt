package com.peekr.domain.file.infrastructure.service.impl

import java.net.URI
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.presigner.S3Presigner

class S3PresignerFactory {
    /**
     * Presign URL을 생성하기 위한 객체를 생성한다.
     *
     * 반드시, 사용 후 **`close()`** 호출
     */
    fun createS3Presigner(
        accessKey: String,
        secretKey: String,
        region: Region,
        endpoint: URI,
    ): S3Presigner = S3Presigner
        .builder()
        .credentialsProvider { AwsBasicCredentials.create(accessKey, secretKey) }
        .region(region)
        .endpointOverride(endpoint)
        .build()
}
