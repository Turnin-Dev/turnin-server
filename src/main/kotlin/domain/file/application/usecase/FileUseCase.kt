package com.peekr.domain.file.application.usecase

import com.peekr.domain.file.application.dto.UploadFileInfoDto
import com.peekr.domain.file.exception.FileException

interface FileUseCase {
    /**
     * `Presigned URL`와 클라이언트가 필요한 정보를 제공한다.
     *
     * `Presigned URL`을 통해 클라이언트가 직접 파일을 업로드해서 서버의 부하를 줄인다.
     *
     * - `Presigned URL`: 토큰 비밀번호를 공개하지 않고 버킷에 직접 액세스할 수 있도록 하는 S3 개념
     *
     * @param fileName 파일 이름
     * @param mimeType MIME 타입 (파일이나 데이터의 형식을 지정하는 2부분으로 이루어진 식별자)
     *
     * @throws FileException.InvalidS3PresignerArgument 잘못된 인자 값(버킷이름, 키 등) 사용 시 발생 - (Global ExceptionHandler에서 자동 처리)
     */
    fun createPresignedUrl(
        fileName: String,
        mimeType: String,
    ): UploadFileInfoDto
}
