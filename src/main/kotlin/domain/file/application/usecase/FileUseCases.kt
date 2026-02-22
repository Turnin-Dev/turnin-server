package com.peekr.domain.file.application.usecase

data class FileUseCases(
    /**
     * 파일 업로드 URL 가져오기
     *
     * @see [GetFileUploadUrlUseCase]
     */
    val getFileUploadUrl: GetFileUploadUrlUseCase,
    /**
     * 파일 업데이트 URL 가져오기
     *
     * @see [GetFileUpdateUrlUseCase]
     */
    val getFileUpdateUrl: GetFileUpdateUrlUseCase,
)
