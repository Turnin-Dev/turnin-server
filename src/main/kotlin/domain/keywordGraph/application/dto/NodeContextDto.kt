package com.peekr.domain.keywordGraph.application.dto

/**
 * NodeContext DTO
 *
 * @property userNodeDto 사용자 노드 DTO
 * @property keywordNodeDtoList 키워드 노드 DTO 리스트
 */
data class NodeContextDto(
    val userNodeDto: UserNodeDto,
    val keywordNodeDtoList: List<KeywordNodeDto>,
)
