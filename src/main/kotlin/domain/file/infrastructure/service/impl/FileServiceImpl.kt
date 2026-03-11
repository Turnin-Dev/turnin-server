package com.peekr.domain.file.infrastructure.service.impl

import com.peekr.domain.file.domain.model.UploadFileInfo
import com.peekr.domain.file.domain.service.FileService
import com.peekr.domain.file.exception.FileException
import java.net.URI
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.services.s3.model.S3Exception

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
        } catch (e: SdkClientException) {
            throw FileException.S3CredentialException(e)
        }
    }

    override fun createPresignedUpdateUrlWithInfo(
        newFileName: String,
        mimeType: String,
    ): UploadFileInfo {
        try {
            val presignedRequest = r2Service.createPresignedRequest(newFileName, mimeType)
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
        } catch (e: SdkClientException) {
            throw FileException.S3CredentialException(e)
        }
    }

    override fun deleteFile(fileName: String) {
        try {
            val parsedFileName = parseFileName(fileName)
            r2Service.deleteFile(parsedFileName)
        } catch (e: S3Exception) {
            throw FileException.R2DeleteFailed(e)
        } catch (e: SdkClientException) {
            throw FileException.R2DeleteFailed(e)
        }
    }

    private fun parseFileName(imageUrl: String): String =
        URI(imageUrl).path.drop(1)
}
