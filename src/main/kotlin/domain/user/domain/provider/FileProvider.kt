package com.turnin.domain.user.domain.provider

import com.turnin.domain.file.domain.model.FileCategory

/**
 * 외부에서 제공되는 File BC API 인터페이스
 */
interface FileProvider {
    /**
     * 파일 삭제
     *
     * @param fileName 파일명
     * @param fileCategory 파일 카테고리
     */
    fun deleteFile(
        fileName: String,
        fileCategory: FileCategory,
    )
}
