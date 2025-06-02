package domain.model.entity.auth

data class JwtToken(
    val accessToken: String,
    val refreshToken: String,
)
