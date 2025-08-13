package com.peekr.domain.user

import com.peekr.common.db.scheme.SocialLoginProvider
import com.peekr.common.db.scheme.UserEntity
import com.peekr.common.db.scheme.UserRole
import com.peekr.domain.user.application.dto.UserDto

object UserTestDoubles {
    fun getUserEntity() = UserEntity.new {
        this.role = UserRole.USER
        this.provider = SocialLoginProvider.GOOGLE
        this.providerId = "123901239"
        this.displayId = "hong_gd_123"
        this.name = "honggd"
        this.profileImageUrl = "https://example.com/image.jpg"
        this.introduce = "hello world!"
    }

    val MockUserDto = UserDto(
        id = 1L,
        role = "USER",
        provider = "GOOGLE",
        providerId = "123901239",
        displayId = "hong_gd_123",
        name = "honggd",
        profileImageUrl = "https://example.com/image.jpg",
        introduce = "hello world!",
    )
}
