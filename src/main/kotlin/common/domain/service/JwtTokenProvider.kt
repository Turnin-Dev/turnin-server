package com.peekr.common.domain.service

import com.peekr.common.domain.entity.JwtToken
import com.peekr.domain.auth.domain.model.entity.AuthUser

interface JwtTokenProvider {
    fun generate(authUser: AuthUser): JwtToken
}
