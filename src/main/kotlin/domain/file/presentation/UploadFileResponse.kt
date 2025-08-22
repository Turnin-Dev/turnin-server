package com.peekr.domain.file.presentation

import kotlinx.serialization.Serializable

/**
 * 파일 업로드 응답 바디
 *
 * @property presignedUrl 미리 정의된 URL
 */
@Serializable
data class UploadFileResponse(val presignedUrl: String) {
    companion object {
        val sample = UploadFileResponse(presignedUrl = "https://example.com/file.jpg")
    }
}
