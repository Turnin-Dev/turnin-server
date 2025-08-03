package com.peekr.domain.auth.presentation.dto

import kotlinx.serialization.Serializable

@Serializable
data class FindUserResultResponse(val isExist: Boolean) {
    companion object {
        val sample = FindUserResultResponse(true)
    }
}
