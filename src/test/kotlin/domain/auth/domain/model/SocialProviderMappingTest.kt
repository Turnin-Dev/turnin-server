package com.peekr.domain.auth.domain.model

import com.peekr.domain.auth.infrastructure.mapper.toSocialLoginProvider
import com.peekr.domain.auth.infrastructure.mapper.toSocialLoginProviderForAuth
import com.peekr.domain.user.infrastructure.mapper.toSocialLoginProvider
import com.peekr.domain.user.infrastructure.mapper.toSocialLoginProviderForUser
import kotlin.test.Test
import org.junit.jupiter.api.assertDoesNotThrow
import com.peekr.common.db.schema.SocialLoginProvider as DbProvider
import com.peekr.domain.auth.domain.model.SocialLoginProviderForAuth as AuthProvider
import com.peekr.domain.user.domain.model.SocialLoginProviderForUser as UserProvider

class SocialProviderMappingTest {
    @Test
    fun `DB - Auth 매핑 전 케이스`() {
        DbProvider.entries.forEach { p ->
            assertDoesNotThrow { p.toSocialLoginProviderForAuth() }
        }
    }

    @Test
    fun `DB - User 매핑 전 케이스`() {
        DbProvider.entries.forEach { p ->
            assertDoesNotThrow { p.toSocialLoginProviderForUser() }
        }
    }

    @Test
    fun `Auth - DB 매핑 전 케이스`() {
        AuthProvider.entries.forEach { p ->
            assertDoesNotThrow { p.toSocialLoginProvider() }
        }
    }

    @Test
    fun `User - DB 매핑 전 케이스`() {
        UserProvider.entries.forEach { p ->
            assertDoesNotThrow { p.toSocialLoginProvider() }
        }
    }
}
