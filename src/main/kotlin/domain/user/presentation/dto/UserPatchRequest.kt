package com.peekr.domain.user.presentation.dto

import com.peekr.common.model.DisplayId
import com.peekr.common.model.Introduce
import com.peekr.common.model.Name
import com.peekr.domain.user.application.dto.UserPatchDto
import kotlinx.serialization.Serializable

/**
 * UserPatch 요청 바디
 *
 * @param displayId 사용자 표시 ID
 * @param name 사용자 이름
 * @param profileImageUrl 사용자 프로필 이미지 url
 * @param introduce 사용자 소개 글
 */
@Serializable
data class UserPatchRequest(
    val displayId: String,
    val name: String,
    val profileImageUrl: String?,
    val introduce: String,
) {
    companion object {
        val sample = UserPatchRequest(
            displayId = "hong_gd_123",
            name = "honggd",
            profileImageUrl = "http://example.com/image.jpg",
            introduce = "hello world!",
        )
    }
}

fun UserPatchRequest.toDto(): UserPatchDto =
    UserPatchDto(
        displayId = DisplayId(displayId),
        name = Name(name),
        profileImageUrl = profileImageUrl,
        introduce = Introduce(introduce),
    )
