package com.peekr.domain.friend.application.usecase.integration

import com.peekr.common.db.schema.UserEntity
import com.peekr.common.db.schema.Users
import com.peekr.common.db.suspendTransaction
import com.peekr.common.model.id.UserId
import com.peekr.domain.friend.domain.provider.ExternalUserInfo
import com.peekr.domain.friend.domain.provider.UserProvider
import com.peekr.domain.user.infrastructure.mapper.UserMapper.toDomain
import org.jetbrains.exposed.sql.intLiteral

class FakeUserProvider : UserProvider {
    override suspend fun existsUser(userId: UserId): Boolean = suspendTransaction {
        Users
            .select(intLiteral(1))
            .where { Users.id eq userId.value }
            .limit(1)
            .any()
    }

    override suspend fun getUserInfos(userIds: List<UserId>): List<ExternalUserInfo> = suspendTransaction {
        val users = UserEntity
            .find { Users.id inList userIds.map { it.value } }
            .map { it.toDomain() }
        users.map { user ->
            ExternalUserInfo(
                userId = user.id,
                displayId = user.displayId,
                userName = user.userName,
                profileImageUrl = user.profileImageUrl,
            )
        }
    }
}
