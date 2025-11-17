package com.peekr.domain.auth.presentation.dto

import com.peekr.domain.auth.application.dto.FindUserResultDto
import kotlinx.serialization.Serializable

/** 사용자 찾기 결과 응답 바디 */
@Serializable
data class ExistsResultResponse(val exists: Boolean) {
    companion object {
        val sample = ExistsResultResponse(true)
    }
}

// ------------------------------ Mapper ------------------------------
fun FindUserResultDto.toResponse(): ExistsResultResponse = ExistsResultResponse(exists)
