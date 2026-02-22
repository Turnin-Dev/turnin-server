package com.peekr.domain.user.presentation.dto

import com.peekr.domain.user.application.dto.UserPatchDto
import kotlinx.serialization.Serializable

/**
 * UserPatch 요청 바디
 *
 * @param name 사용자 이름
 * @param displayId 사용자 표시 ID
 * @param oldProfileImageUrl 기존 프로필 사진 URL
 * @param newProfileImageUrl 새로운 프로필 사진 URL
 * @param introduce 사용자 소개 글
 */
@Serializable
data class UserPatchRequest(
    val name: String,
    val displayId: String,
    val oldProfileImageUrl: String?,
    val newProfileImageUrl: String?,
    val introduce: String,
) {
    companion object {
        val sample = UserPatchRequest(
            name = "홍길동",
            displayId = "hongg",
            oldProfileImageUrl = "http://example.com/image.jpg",
            newProfileImageUrl = "http://example.com/new-image.jpg",
            introduce = "hello world!",
        )
    }
}

fun UserPatchRequest.toDto(): UserPatchDto =
    UserPatchDto(
        userName = name,
        displayId = displayId,
        oldProfileImageUrl = oldProfileImageUrl,
        newProfileImageUrl = newProfileImageUrl,
        introduce = introduce,
    )
