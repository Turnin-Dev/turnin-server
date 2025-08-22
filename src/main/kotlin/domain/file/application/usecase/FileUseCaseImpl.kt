package com.peekr.domain.file.application.usecase

import com.peekr.domain.file.domain.service.FileService

class FileUseCaseImpl(private val fileService: FileService) : FileUseCase {
    override fun createPresignedUrl(fileName: String): String =
        fileService.createPresignedUrl(fileName)
}
