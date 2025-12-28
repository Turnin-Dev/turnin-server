package com.peekr.domain.keywordGraph.presentation.dto

import com.peekr.domain.keywordGraph.application.dto.NodeContextDto
import kotlinx.serialization.Serializable

/**
 * NodeContext 응답 바디
 *
 * @property userNode 사용자 노드 응답 바디
 * @property keywordNodes 키워드 노드 응답 바디 리스트
 */
@Serializable
data class NodeContextResponse(
    val userNode: UserNodeResponse,
    val keywordNodes: List<KeywordNodeResponse>,
)

fun NodeContextDto.toResponse(): NodeContextResponse =
    NodeContextResponse(
        userNode = userNodeDto.toResponse(),
        keywordNodes = keywordNodeDtoList.map { it.toResponse() },
    )
