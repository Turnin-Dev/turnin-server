package com.peekr.domain.file.application.usecase

import com.peekr.domain.file.application.dto.UploadFileInfoDto
import com.peekr.domain.file.application.dto.toDto
import com.peekr.domain.file.domain.service.FileService

/**
 * 파일 업데이트 URL 가져오기
 *
 * @see invoke
 */
class GetFileUpdateUrlUseCase(private val fileService: FileService) {
    /**
     * 파일 업데이트 URL과 클라이언트가 필요한 정보를 가져온다.
     *
     * **파일 업데이트 후에는 반드시 기존 파일을 삭제하고 새 파일을 DB에 업데이트하는 후처리 작업이 필요하다.**
     *
     * @param newFileName 업로드할 파일명
     * @param mimeType MIME 타입
     */
    operator fun invoke(newFileName: String, mimeType: String): UploadFileInfoDto =
        fileService.createPresignedUpdateUrlWithInfo(newFileName, mimeType).toDto()
}
