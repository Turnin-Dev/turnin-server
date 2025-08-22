package com.peekr.domain.file.infrastructure.service.impl

import com.peekr.domain.file.domain.model.UploadFileInfo
import com.peekr.domain.file.domain.service.FileService

class FileServiceImpl(private val r2Service: CloudflareR2Service) : FileService {
    override fun createPresignedUrlWithInfo(fileName: String): UploadFileInfo {
        val presignedRequest = r2Service.createPresignedRequest(fileName)
        val uploadFileInfo = UploadFileInfo(
            presignedUrl = presignedRequest.url().toString(),
            method = presignedRequest.httpRequest().method().name,
            headers = presignedRequest.httpRequest().headers().mapValues { it.value.first() },
            expiresInSeconds = r2Service.signatureDuration.seconds,
        )
        return uploadFileInfo
    }
}
