package com.turnin.domain.user.presentation.dto

import kotlinx.serialization.Serializable

/**
 * 사용자 소개글 요청 바디
 *
 * @property introduce 수정할 소개글
 */
@Serializable
data class IntroducePatchRequest(val introduce: String) {
    companion object {
        val sample = IntroducePatchRequest(introduce = "hello world!")
    }
}
