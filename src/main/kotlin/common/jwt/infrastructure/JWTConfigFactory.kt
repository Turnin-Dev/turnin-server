package com.peekr.common.jwt.infrastructure

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.peekr.common.jwt.domain.model.entity.JWTVerifierConfig
import com.peekr.common.jwt.exception.TokenException
import org.koin.core.annotation.Singleton

@Singleton
class JWTConfigFactory {
    fun createAlgorithm(secretKey: String): Algorithm = Algorithm.HMAC256(secretKey)

    fun createVerifier(verifierConfig: JWTVerifierConfig): JWTVerifier = try {
        val algorithm = createAlgorithm(verifierConfig.secretKey)

        JWT
            .require(algorithm)
            .withAudience(verifierConfig.audience)
            .withIssuer(verifierConfig.issuer)
            .build()
    } catch (e: Exception) {
        throw TokenException.InvalidTokenException("Invalid token")
    }
}
