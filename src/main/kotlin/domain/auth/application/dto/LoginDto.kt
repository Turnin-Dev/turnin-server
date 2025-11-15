package com.peekr.domain.auth.application.dto

import com.peekr.common.model.SocialLoginProvider

/** 애플리케이션 계층에서 사용하는 LoginDto */
data class LoginDto(
    val provider: SocialLoginProvider,
    val providerId: String,
)
