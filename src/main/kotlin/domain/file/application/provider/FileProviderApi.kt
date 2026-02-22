package com.peekr.domain.file.application.provider

import com.peekr.domain.file.application.usecase.DeleteFileUseCase

/**
 * 외부에 제공할 File API
 */
class FileProviderApi(private val deleteFileUseCase: DeleteFileUseCase) {
    /**
     * 파일을 서버에서 직접 삭제한다.
     *
     * @see DeleteFileUseCase
     */
    fun deleteFile(fileName: String) = deleteFileUseCase(fileName)
}
