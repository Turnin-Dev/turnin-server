package com.peekr.domain.user

import com.peekr.common.db.scheme.UserEntity
import com.peekr.domain.auth.domain.model.SocialLoginProvider

object UserTestDoubles {
    fun getUserEntity() = UserEntity.new {
        this.provider = SocialLoginProvider.Google
        this.providerId = "123901239"
        this.name = "honggd"
        this.nickname = "hongdddddddd"
        this.profileImageUrl = "https://example.com/image.jpg"
        this.introduce = "hello world!"
    }
}
