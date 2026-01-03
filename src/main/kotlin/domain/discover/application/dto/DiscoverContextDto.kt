package com.peekr.domain.discover.application.dto

/**
 * 탐색 컨텍스트 DTO
 *
 * 탐색에 필요한 정보를 담고 있다.
 *
 * - 담고 있는 정보: 사용자 정보 일부 + 키워드 정보 일부
 *
 * @property user 탐색용 사용자
 * @property keywords 탐색용 키워드 리스트
 */
data class DiscoverContextDto(
    val user: DiscoverUserDto,
    val keywords: List<DiscoverKeywordDto>,
)
