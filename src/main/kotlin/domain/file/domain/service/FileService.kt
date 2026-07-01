package com.turnin.domain.file.domain.service

import com.turnin.domain.file.domain.model.UploadFileInfo
import com.turnin.domain.file.exception.FileException

/**
 * 파일 서비스
 */
interface FileService {
    /**
     * 업로드용 `Presigned URL`와 클라이언트가 필요한 정보를 제공한다.
     *
     * `Presigned URL`을 통해 클라이언트가 직접 파일을 업로드해서 서버의 부하를 줄인다.
     *
     * - `Presigned URL`: 토큰 비밀번호를 공개하지 않고 버킷에 직접 액세스할 수 있도록 하는 S3 개념
     *
     * @param fileName 파일명
     * @param mimeType MIME 타입 (파일이나 데이터의 형식을 지정하는 2부분으로 이루어진 식별자)
     *
     * @throws FileException.InvalidS3PresignerArgument 잘못된 인자 값(버킷이름, 키 등) 사용 시 발생 - (Global ExceptionHandler에서 자동 처리)
     * @throws FileException.S3CredentialException 서명 생성 과정에서 에러 발생 시 예외가 발생한다.
     */
    suspend fun createPresignedUrlWithInfo(
        fileName: String,
        mimeType: String,
    ): UploadFileInfo

    /**
     * 업데이트용 `Presigned URL`와 클라이언트가 필요한 정보를 제공한다.
     *
     * **클라이언트는 반드시 새 파일을 업로드 완료한 뒤 [deleteFile]를 호출해야 한다.**
     *
     * @param newFileName 새 파일명
     * @param mimeType MIME 타입 (파일이나 데이터의 형식을 지정하는 2부분으로 이루어진 식별자)
     *
     * @throws FileException.InvalidS3PresignerArgument 잘못된 인자 값(버킷이름, 키 등) 사용 시 발생 - (Global ExceptionHandler에서 자동 처리)
     * @throws FileException.S3CredentialException 서명 생성 과정에서 에러 발생 시 예외가 발생한다.
     */
    suspend fun createPresignedUpdateUrlWithInfo(
        newFileName: String,
        mimeType: String,
    ): UploadFileInfo

    /**
     * 파일을 서버에서 직접 삭제한다.
     *
     * 이 메서드를 호출하는 상황
     * 1. 단순 파일 삭제 호출
     * 2. 업데이트 완료 확정 시 호출: 파일 업데이트 완료를 확정하기 위해 호출하며, 새 파일 업로드가 완료된 후에 수행한다.
     * (또한, 이 메서드를 호출하는 부분에서 `DB 경로 업데이트`와 `기존 파일 삭제`를 수행해야 한다.)
     *
     * @param fileUrl 삭제할 파일 URL
     *
     * @throws FileException.R2DeleteFailed 파일을 삭제하는 과정에서 에러가 발생하면 예외가 발생한다.
     */
    suspend fun deleteFile(fileUrl: String)
}
