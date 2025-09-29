package com.peekr.domain.user

import com.peekr.common.db.schema.Role
import com.peekr.common.db.schema.SocialLoginProvider
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.model.DisplayId
import com.peekr.common.model.Name
import com.peekr.common.model.UserId
import com.peekr.domain.user.application.dto.UserDto
import com.peekr.domain.user.domain.model.RoleForUser
import com.peekr.domain.user.domain.model.SocialLoginProviderForUser

object UserTestDoubles {
    fun getUserEntity() = UserEntity.new {
        this.role = Role.USER
        this.provider = SocialLoginProvider.GOOGLE
        this.providerId = "123901239"
        this.displayId = "hong_gd_123"
        this.name = "honggd"
        this.profileImageUrl = "https://example.com/image.jpg"
        this.introduce = "hello world!"
    }

    val MockUserDto = UserDto(
        id = UserId(1L),
        role = RoleForUser.USER,
        provider = SocialLoginProviderForUser.GOOGLE,
        providerId = "123901239",
        displayId = DisplayId("hong_gd_123"),
        name = Name("honggd"),
        profileImageUrl = "https://example.com/image.jpg",
        introduce = "hello world!",
        isActive = true,
        lastLoginAt = null,
    )
}
