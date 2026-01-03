package com.peekr.domain.discover.domain.model

/**
 * 탐색 컨텍스트 모델
 *
 * 탐색에 필요한 정보를 담고 있다.
 *
 * - 담고 있는 정보: 사용자 정보 일부 + 키워드 정보 일부
 *
 * @property user 탐색용 사용자
 * @property keywords 탐색용 키워드 리스트
 */
data class DiscoverContext(
    val user: DiscoverUser,
    val keywords: List<DiscoverKeyword>,
)
