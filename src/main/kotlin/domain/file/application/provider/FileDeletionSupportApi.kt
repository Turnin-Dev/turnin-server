package com.turnin.domain.file.application.provider

import com.turnin.domain.file.application.usecase.DeleteFileUseCase

/**
 * 외부에 제공할 File 삭제 제공 API
 */
class FileDeletionSupportApi(private val deleteFileUseCase: DeleteFileUseCase) {
    /**
     * 파일을 서버에서 직접 삭제한다.
     *
     * @param fileUrl 파일 URL
     *
     * @see DeleteFileUseCase
     */
    suspend fun deleteFile(
        fileUrl: String,
    ) = deleteFileUseCase(fileUrl)
}
