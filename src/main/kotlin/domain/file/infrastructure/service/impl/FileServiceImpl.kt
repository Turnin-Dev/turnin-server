package com.peekr.domain.file.infrastructure.service.impl

import com.peekr.domain.file.domain.model.UploadFileInfo
import com.peekr.domain.file.domain.service.FileService
import com.peekr.domain.file.exception.FileException

class FileServiceImpl(private val r2Service: CloudflareR2Service) : FileService {
    override fun createPresignedUrlWithInfo(fileName: String, mimeType: String): UploadFileInfo {
        try {
            val presignedRequest = r2Service.createPresignedRequest(fileName, mimeType)
            val uploadFileInfo = UploadFileInfo(
                presignedUrl = presignedRequest.url().toString(),
                method = presignedRequest.httpRequest().method().name,
                expiresInSeconds = r2Service.signatureDuration.seconds,
            )
            return uploadFileInfo
        } catch (e: IllegalArgumentException) {
            throw FileException.InvalidS3PresignerArgument(e)
        } catch (e: IllegalStateException) {
            throw FileException.InvalidS3PresignerArgument(e)
        }
    }
}
