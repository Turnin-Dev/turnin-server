package com.turnin.domain.auth.application.usecase

data class AuthAdminUseCases(
    /**
     * 회원가입
     * @see RegisterUseCase
     */
    val register: RegisterUseCase,
    /**
     * 사용자 표시 ID 존재 여부 확인
     * @see ExistsDisplayIdUseCase
     */
    val existsDisplayId: ExistsDisplayIdUseCase,
    /**
     * 관리자 비밀키 검사
     * @see ValidateAdminSecretKeyUseCase
     */
    val validateAdminSecretKey: ValidateAdminSecretKeyUseCase,
)
