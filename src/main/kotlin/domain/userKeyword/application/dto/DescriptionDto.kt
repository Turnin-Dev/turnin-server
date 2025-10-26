package com.peekr.domain.userKeyword.application.dto

import com.peekr.domain.userKeyword.domain.model.Description

/**
 * 사용자별 키워드 설명 DTO
 *
 * @property value 설명
 */
data class DescriptionDto(val value: String?)

fun Description.toDto(): DescriptionDto = DescriptionDto(value)

fun DescriptionDto.toDomain(): Description = Description(value)
