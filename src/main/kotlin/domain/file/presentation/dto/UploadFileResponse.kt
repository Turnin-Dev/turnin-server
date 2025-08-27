package com.peekr.domain.file.presentation.dto

import com.peekr.domain.file.application.dto.UploadFileInfoDto
import kotlinx.serialization.Serializable

/**
 * 파일 업로드 응답 바디
 *
 * @property presignedUrl 사전 정의된 URL
 * @property method HTTP 메서드 (Ex. PUT)
 * @property expiresInSeconds 만료 시간 (초 기준)
 */
@Serializable
data class UploadFileResponse(
    val presignedUrl: String,
    val method: String,
    val expiresInSeconds: Long,
) {
    companion object {
        val sample = UploadFileResponse(
            presignedUrl = "https://example-storage.com/objects/my-image.jpg",
            method = "PUT",
            expiresInSeconds = 600,
        )
    }
}

fun UploadFileInfoDto.toResponse() = UploadFileResponse(
    presignedUrl = presignedUrl,
    method = method,
    expiresInSeconds = expiresInSeconds,
)
