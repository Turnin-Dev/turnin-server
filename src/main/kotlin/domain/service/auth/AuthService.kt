package com.peekr.domain.service.auth

import com.peekr.domain.model.entity.auth.AuthUser
import com.peekr.domain.model.entity.auth.JwtToken
import com.peekr.domain.model.value.auth.SocialLoginProvider

interface AuthService {
    /**
     * 소셜로그인
     *
     * [provider]와 [providerId]로 유저를 조회하고 로그인을 진행한다.
     * 만약, 계정이 존재하지 않는다면 [register]를 통해 회원가입을 진행한다.
     */
    suspend fun login(
        provider: SocialLoginProvider,
        providerId: String,
    ): JwtToken?

    /**
     * 회원가입
     *
     * 단, 회원가입은 기존 회원이 존재하지 않는다는 가정하에 진행된다.
     *
     * @param authUser [AuthUser]
     */
    suspend fun register(authUser: AuthUser): JwtToken
}
