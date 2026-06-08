package com.turnin.domain.auth.application.usecase

data class AuthUseCases(
    /**
     * 로그인
     * @see LoginUseCase
     */
    val login: LoginUseCase,
    /**
     * 회원가입
     * @see RegisterUseCase
     */
    val register: RegisterUseCase,
    /**
     * 리프레쉬 토큰 갱신
     * @see RefreshTokenUseCase
     */
    val refresh: RefreshTokenUseCase,
    /**
     * 사용자 표시 ID 존재 여부 확인
     * @see ExistsDisplayIdUseCase
     */
    val existsDisplayId: ExistsDisplayIdUseCase,
    /**
     * 사용자 찾기
     * @see FindUserUseCase
     */
    val findUser: FindUserUseCase,
)
