package com.peekr.domain.file.application.usecase

import com.peekr.domain.file.domain.service.FileService

/**
 * 파일 삭제
 *
 * @see invoke
 */
class DeleteFileUseCase(private val fileService: FileService) {
    /**
     * 파일을 서버에서 직접 삭제한다.
     *
     * @param fileName 삭제할 파일명
     *
     * @see [FileService.deleteFile]
     */
    operator fun invoke(fileName: String) =
        fileService.deleteFile(fileName)
}
