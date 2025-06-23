package com.peekr.domain.user

import com.peekr.common.db.scheme.UserEntity
import com.peekr.domain.auth.domain.model.SocialLoginProvider
import com.peekr.domain.user.application.dto.UserDto

object UserTestDoubles {
    fun getUserEntity() = UserEntity.new {
        this.provider = SocialLoginProvider.Google
        this.providerId = "123901239"
        this.name = "honggd"
        this.nickname = "hongdddddddd"
        this.profileImageUrl = "https://example.com/image.jpg"
        this.introduce = "hello world!"
    }

    val MockUserDto = UserDto(
        id = 1L,
        provider = "Google",
        providerId = "123901239",
        name = "honggd",
        nickname = "hongdddd",
        profileImageUrl = "https://example.com/image.jpg",
        introduce = "hello world!",
    )
}
