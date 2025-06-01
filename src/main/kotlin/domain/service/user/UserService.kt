package com.peekr.domain.service.user

import domain.model.user.User

interface UserService {
    /**
     * 소셜로그인
     *
     * [provider]와 [providerId]로 유저를 조회하고 로그인을 진행한다.
     * 만약, 계정이 존재하지 않는다면 [register]를 통해 회원가입을 진행한다.
     */
    suspend fun login(
        provider: String,
        providerId: String,
    ): User?

    /**
     * 회원가입
     *
     * @param name 이름
     * @param nickname 닉네임
     * @param profileImageUrl 이미지 URL
     * @param introduce 소개 글
     */
    suspend fun register(
        name: String,
        nickname: String,
        profileImageUrl: String?,
        introduce: String?,
    ): User
}
