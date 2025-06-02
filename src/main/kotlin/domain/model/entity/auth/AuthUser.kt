package domain.model.entity.auth

import com.peekr.domain.model.value.auth.SocialLoginProvider

data class AuthUser(
    val id: Long,
    val provider: SocialLoginProvider,
    val providerId: String,
    val name: String,
    val nickname: String,
    val profileImageUrl: String?,
    val introduce: String?,
)
