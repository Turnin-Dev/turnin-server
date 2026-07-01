package com.turnin.domain.user.domain.provider

/**
 * 외부에서 제공되는 File BC API 인터페이스
 */
interface FileProvider {
    /**
     * 파일 삭제
     *
     * @param fileUrl 파일 URL
     */
    fun deleteFile(fileUrl: String)
}
