package domain.model.user

import com.peekr.domain.model.value.user.SocialLoginProvider

data class User(
    val id: Long,
    val provider: SocialLoginProvider,
    val providerId: String,
    val name: String,
    val nickname: String,
    val profileImageUrl: String?,
    val introduce: String?,
)
