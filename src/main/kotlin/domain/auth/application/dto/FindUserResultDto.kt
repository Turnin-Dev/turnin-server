package com.turnin.domain.auth.application.dto

/**
 * 사용자 존재 확인 결과 DTO
 *
 * @property exists 사용자 존재 여부
 */
data class FindUserResultDto(val exists: Boolean)
