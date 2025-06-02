package com.peekr.domain.service.auth

import domain.model.entity.auth.AuthUser
import domain.model.entity.auth.JwtToken

interface JwtTokenProvider {
    fun generate(authUser: AuthUser): JwtToken
}
