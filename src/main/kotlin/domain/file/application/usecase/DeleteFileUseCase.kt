package com.turnin.domain.file.application.usecase

import com.turnin.domain.file.domain.model.FileCategory
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
     * @param fileName 삭제할 파일명
     * @param fileCategory 파일 카테고리
     *
     * @see [FileService.deleteFile]
     */
    operator fun invoke(
        fileName: String,
        fileCategory: FileCategory,
    ) {
        val fileFullName = "${fileCategory.prefix}/$fileName"
        return fileService.deleteFile(fileFullName)
    }
}
