package com.peekr.domain.file.application.usecase

import com.peekr.domain.file.application.dto.UploadFileInfoDto
import com.peekr.domain.file.application.dto.toDto
import com.peekr.domain.file.domain.service.FileService

class FileUseCase(private val fileService: FileService) {
    operator fun invoke(fileName: String, mimeType: String): UploadFileInfoDto =
        fileService.createPresignedUrlWithInfo(fileName, mimeType).toDto()
}
