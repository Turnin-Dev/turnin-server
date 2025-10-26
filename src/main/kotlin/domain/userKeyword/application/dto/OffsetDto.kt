package com.peekr.domain.userKeyword.application.dto

import com.peekr.domain.userKeyword.domain.model.Offset

/**
 * 오프셋 DTO
 *
 * @property x 오프셋 X
 * @property y 오프셋 Y
 */
data class OffsetDto(
    val x: Float,
    val y: Float,
)

fun Offset.toDto(): OffsetDto = OffsetDto(x, y)

fun OffsetDto.toDomain(): Offset = Offset(x, y)
