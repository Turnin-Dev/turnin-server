package com.turnin.domain.file.application.usecase

import com.turnin.domain.file.application.dto.UploadFileInfoDto
import com.turnin.domain.file.application.dto.toDto
import com.turnin.domain.file.domain.model.FileCategory
import com.turnin.domain.file.domain.service.FileService

/**
 * 파일 업로드 URL 가져오기
 *
 * @see invoke
 */
class GetFileUploadUrlUseCase(private val fileService: FileService) {
    /**
     * 파일 업로드 URL과 클라이언트가 필요한 정보를 가져온다.
     *
     * @param fileName 업로드할 파일명
     * @param mimeType MIME 타입
     * @param fileCategory 파일 카테고리 (기본 값은 프로필 사진)
     */
    operator fun invoke(
        fileName: String,
        mimeType: String,
        fileCategory: FileCategory = FileCategory.PROFILE_IMAGE,
    ): UploadFileInfoDto {
        val fileFullName = "${fileCategory.prefix}/$fileName"
        return fileService.createPresignedUrlWithInfo(fileFullName, mimeType).toDto()
    }
}
