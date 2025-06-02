package com.peekr.domain.service.auth

import com.peekr.domain.model.entity.auth.AuthUser
import com.peekr.domain.model.entity.auth.JwtToken

interface JwtTokenProvider {
    fun generate(authUser: AuthUser): JwtToken
}
