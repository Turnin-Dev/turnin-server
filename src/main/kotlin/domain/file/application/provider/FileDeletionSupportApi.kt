package com.turnin.domain.file.application.provider

import com.turnin.domain.file.application.usecase.DeleteFileUseCase
import com.turnin.domain.file.domain.model.FileCategory

/**
 * 외부에 제공할 File 삭제 제공 API
 */
class FileDeletionSupportApi(private val deleteFileUseCase: DeleteFileUseCase) {
    /**
     * 파일을 서버에서 직접 삭제한다.
     *
     * @see DeleteFileUseCase
     */
    fun deleteFile(
        fileName: String,
        fileCategory: FileCategory,
    ) = deleteFileUseCase(fileName, fileCategory)
}
