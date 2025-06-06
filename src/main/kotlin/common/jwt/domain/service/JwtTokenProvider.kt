package com.peekr.common.jwt.domain.service

import com.peekr.common.jwt.domain.model.entity.JwtToken
import com.peekr.domain.auth.domain.model.entity.AuthUser

interface JwtTokenProvider {
    fun generate(authUser: AuthUser): JwtToken
}
