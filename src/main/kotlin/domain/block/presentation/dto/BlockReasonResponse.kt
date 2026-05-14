package com.turnin.domain.block.presentation.dto

import com.turnin.domain.block.application.dto.BlockReasonDto
import kotlinx.serialization.Serializable

/**
 * 차단 사유 응답 바디
 *
 * @property id 차단 사유 ID
 * @property code 차단 사유 코드
 * @property description 차단 사유 설명
 */
@Serializable
data class BlockReasonResponse(
    val id: Long,
    val code: String,
    val description: String,
) {
    companion object {
        val sampleList = List(2) {
            val id = it + 1L
            BlockReasonResponse(
                id = id,
                code = "sample $id",
                description = "sample description $id",
            )
        }
    }
}

fun BlockReasonDto.toResponse(): BlockReasonResponse =
    BlockReasonResponse(
        id = id,
        code = code,
        description = description,
    )

fun List<BlockReasonDto>.toResponse(): List<BlockReasonResponse> =
    this.map { it.toResponse() }
