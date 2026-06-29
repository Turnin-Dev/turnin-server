package com.turnin.domain.file.application.usecase

import com.turnin.domain.file.domain.service.FileService

/**
 * 파일 삭제
 *
 * @see invoke
 */
class DeleteFileUseCase(private val fileService: FileService) {
    /**
     * 파일을 서버에서 직접 삭제한다.
     *
     * @param fileUrl 삭제할 파일 URL
     *
     * @see [FileService.deleteFile]
     */
    operator fun invoke(
        fileUrl: String,
    ) = fileService.deleteFile(fileUrl)
}
