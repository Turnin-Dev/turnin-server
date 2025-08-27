package com.peekr.domain.file.infrastructure.service.impl

import com.peekr.domain.file.domain.model.UploadFileInfo
import com.peekr.domain.file.domain.service.FileService

class FileServiceImpl(private val r2Service: CloudflareR2Service) : FileService {
    override fun createPresignedUrlWithInfo(fileName: String, mimetype: String): UploadFileInfo {
        val presignedRequest = r2Service.createPresignedRequest(fileName, mimetype)
        val uploadFileInfo = UploadFileInfo(
            presignedUrl = presignedRequest.url().toString(),
            method = presignedRequest.httpRequest().method().name,
            expiresInSeconds = r2Service.signatureDuration.seconds,
        )
        return uploadFileInfo
    }
}
