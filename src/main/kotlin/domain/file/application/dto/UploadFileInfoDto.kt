package com.peekr.domain.file.application.dto

import com.peekr.domain.file.domain.model.UploadFileInfo

/**
 * 사전 정의된 URL과 함께 클라이언트가 필요한 정보
 *
 * @property presignedUrl 사전 정의된 URL
 * @property method HTTP 메서드 (Ex. PUT)
 * @property expiresInSeconds 만료 시간 (초 기준)
 */
data class UploadFileInfoDto(
    val presignedUrl: String,
    val method: String,
    val expiresInSeconds: Long,
)

fun UploadFileInfo.toDto(): UploadFileInfoDto = UploadFileInfoDto(
    presignedUrl = presignedUrl,
    method = method,
    expiresInSeconds = expiresInSeconds,
)
