package com.peekr.domain.auth.domain.service

import com.peekr.common.jwt.domain.model.JWTToken
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.model.FindUserResult
import com.peekr.domain.auth.domain.model.LoginResult
import com.peekr.domain.auth.domain.model.RegisterResult
import com.peekr.domain.auth.domain.model.SocialLoginProviderForAuth

interface AuthService {
    /**
     * 소셜로그인
     *
     * [provider]와 [providerId]로 유저를 조회하고 로그인을 진행한다.
     * 만약, 계정이 존재하지 않는다면 [register]를 통해 회원가입을 진행한다.
     *
     * @return [LoginResult] 정상적으로 로그인이 진행된 경우 로그인 결과 값 반환
     * (로그인 실패 (사용자를 가져올 수 없는 경우) **`null`** 반환)
     */
    suspend fun login(
        provider: SocialLoginProviderForAuth,
        providerId: String,
    ): LoginResult?

    /**
     * 회원가입
     *
     * 단, 회원가입은 기존 회원이 존재하지 않는다는 가정하에 진행된다.
     *
     * @param authUser [AuthUser]
     */
    suspend fun register(authUser: AuthUser): RegisterResult

    /**
     * 리프레쉬 토큰 갱신
     *
     * @param token 리프레쉬 토큰
     *
     * @return [JWTToken] 정상적으로 리프레쉬 토큰이 갱신된 경우
     * (만약 리프레쉬 토큰 만료시 **`null`** 반환)
     */
    suspend fun refresh(token: String): JWTToken?

    /** [provider]와 [providerId]로 사용자를 찾는다. */
    suspend fun findUser(
        provider: SocialLoginProviderForAuth,
        providerId: String,
    ): FindUserResult
}
