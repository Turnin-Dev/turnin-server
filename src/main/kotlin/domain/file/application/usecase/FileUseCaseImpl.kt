package com.peekr.domain.file.application.usecase

import com.peekr.domain.file.application.dto.UploadFileInfoDto
import com.peekr.domain.file.application.dto.toDto
import com.peekr.domain.file.domain.service.FileService

class FileUseCaseImpl(private val fileService: FileService) : FileUseCase {
    override fun createPresignedUrl(fileName: String): UploadFileInfoDto =
        fileService.createPresignedUrlWithInfo(fileName).toDto()
}
