package com.peekr.domain.user

import com.peekr.common.db.schema.FriendEntity
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.db.schema.Users
import com.peekr.common.model.DisplayId
import com.peekr.common.model.FriendStatus
import com.peekr.common.model.Introduce
import com.peekr.common.model.Name
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.UserId
import com.peekr.common.util.toOffsetDateTime
import com.peekr.domain.user.application.dto.UserDto
import java.time.Instant
import org.jetbrains.exposed.dao.id.EntityID

object UserTestDoubles {
    fun saveAndGetUserEntity(
        providerId: String = "123901239",
        displayId: String = "hong_gd_123",
    ) = UserEntity.new {
        this.role = Role.USER
        this.provider = SocialLoginProvider.GOOGLE
        this.providerId = providerId
        this.displayId = displayId
        this.name = "honggd"
        this.profileImageUrl = "https://example.com/image.jpg"
        this.introduce = "hello world!"
    }

    fun saveFriendEntity(
        requesterId: Long,
        receiverId: Long,
        status: FriendStatus,
    ) {
        FriendEntity.new {
            this.requesterId = EntityID(requesterId, Users)
            this.receiverId = EntityID(receiverId, Users)
            this.status = status
            respondedAt = Instant.now().toOffsetDateTime()
        }
    }

    val MockUserDto = UserDto(
        id = UserId(1L),
        role = Role.USER,
        provider = SocialLoginProvider.GOOGLE,
        providerId = "123901239",
        displayId = DisplayId("hong_gd_123"),
        name = Name("honggd"),
        profileImageUrl = "https://example.com/image.jpg",
        introduce = Introduce("hello world!"),
        isActive = true,
        lastLoginAt = null,
    )
}
