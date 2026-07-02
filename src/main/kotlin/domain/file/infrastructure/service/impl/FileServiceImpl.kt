package com.turnin.domain.file.infrastructure.service.impl

import aws.sdk.kotlin.services.s3.model.S3Exception
import aws.smithy.kotlin.runtime.SdkBaseException
import com.turnin.domain.file.domain.model.UploadFileInfo
import com.turnin.domain.file.domain.service.FileService
import com.turnin.domain.file.exception.FileException
import java.net.URI

class FileServiceImpl(private val r2Service: ImageR2Service) : FileService {
    override suspend fun createPresignedUrlWithInfo(
        fileName: String,
        mimeType: String,
    ): UploadFileInfo {
        try {
            val presignedUrl = r2Service.createPresignedUrl(fileName, mimeType)
            val uploadFileInfo = UploadFileInfo(
                presignedUrl = presignedUrl,
                method = "PUT",
                expiresInSeconds = r2Service.signatureDuration.seconds,
            )
            return uploadFileInfo
        } catch (e: IllegalArgumentException) {
            throw FileException.InvalidS3PresignerArgument(e)
        } catch (e: IllegalStateException) {
            throw FileException.InvalidS3PresignerArgument(e)
        } catch (e: SdkBaseException) {
            throw FileException.S3CredentialException(e)
        }
    }

    override suspend fun createPresignedUpdateUrlWithInfo(
        newFileName: String,
        mimeType: String,
    ): UploadFileInfo {
        try {
            val presignedUrl = r2Service.createPresignedUrl(newFileName, mimeType)
            val uploadFileInfo = UploadFileInfo(
                presignedUrl = presignedUrl,
                method = "PUT",
                expiresInSeconds = r2Service.signatureDuration.seconds,
            )
            return uploadFileInfo
        } catch (e: IllegalArgumentException) {
            throw FileException.InvalidS3PresignerArgument(e)
        } catch (e: IllegalStateException) {
            throw FileException.InvalidS3PresignerArgument(e)
        } catch (e: SdkBaseException) {
            throw FileException.S3CredentialException(e)
        }
    }

    override suspend fun deleteFile(fileUrl: String) {
        try {
            val parsedFileName = parseFileName(fileUrl)
            r2Service.deleteFile(parsedFileName)
        } catch (e: S3Exception) {
            throw FileException.R2DeleteFailed(e)
        } catch (e: SdkBaseException) {
            throw FileException.R2DeleteFailed(e)
        }
    }

    /**
     * 파일명 혹은 파일 URL을 파싱한다.
     *
     * 파라미터 형태가 파일명이라면 그대로 반환하고, URL 형태면 path를 제거하고 파일명 형태로 반환한다.
     *
     * @param fileNameOrUrl 파일명 혹은 파일 URL
     * @return 파일명 (R2 기준으로 키 값, Ex) image.jpeg, images/image.jpeg)
     */
    private fun parseFileName(fileNameOrUrl: String): String {
        val rawKey = fileNameOrUrl.removePrefix("/")
        val parsedPath = runCatching { URI(fileNameOrUrl).path }
            .getOrNull()
            ?.removePrefix("/")

        return parsedPath?.takeIf { it.isNotBlank() } ?: rawKey
    }
}
