package com.peekr.domain.auth.presentation.dto

import kotlinx.serialization.Serializable

/** 사용자 찾기 결과 응답 바디 */
@Serializable
data class FindUserResultResponse(val isExist: Boolean) {
    companion object {
        val sample = FindUserResultResponse(true)
    }
}
