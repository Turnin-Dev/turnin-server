package com.peekr.domain.auth.application.usecase

data class AuthUseCases(
    /** 로그인 */
    val login: LoginUseCase,
    /** 회원가입 */
    val register: RegisterUseCase,
    /** 리프레쉬 토큰 갱신 */
    val refresh: RefreshTokenUseCase,
    /** 사용자 표시 ID 존재 여부 확인 */
    val existsDisplayId: ExistsDisplayIdUseCase,
    /** 사용자 찾기 */
    val findUser: FindUserUseCase,
)
