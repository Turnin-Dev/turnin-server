package com.peekr.domain.user

import com.peekr.common.model.Introduce
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.UserName
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId
import com.peekr.domain.user.application.dto.UserDto

object UserTestDoubles {
    val MockUserDto = UserDto(
        id = UserId(1L),
        role = Role.USER,
        provider = SocialLoginProvider.GOOGLE,
        providerId = "123901239",
        displayId = DisplayId("hong_gd_123"),
        userName = UserName("honggd"),
        profileImageUrl = "https://example.com/image.jpg",
        introduce = Introduce("hello world!"),
        isActive = true,
        lastLoginAt = 1000L,
    )
}
