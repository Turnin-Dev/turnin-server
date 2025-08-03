package com.peekr.domain.auth.application.dto

/**
 * 사용자 존재 확인 결과 DTO
 *
 * @property isExist 사용자 존재 여부
 */
data class FindUserResultDto(val isExist: Boolean)
