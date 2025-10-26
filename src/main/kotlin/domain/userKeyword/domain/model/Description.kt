package com.peekr.domain.userKeyword.domain.model

import com.peekr.domain.userKeyword.application.dto.DescriptionDto

/**
 * 사용자별 키워드 설명
 *
 * @property value 설명
 */
data class Description(val value: String?)

fun Description.toDto(): DescriptionDto = DescriptionDto(value)

fun DescriptionDto.toDomain(): Description = Description(value)
