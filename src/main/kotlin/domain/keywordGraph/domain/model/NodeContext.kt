package com.peekr.domain.keywordGraph.domain.model

/**
 * 키워드 네트워크 그래프에서의 한 단위
 *
 * @property userNode 사용자 노드
 * @property keywordNodes 키워드 노드 리스트
 */
data class NodeContext(
    val userNode: UserNode,
    val keywordNodes: List<KeywordNode>,
)
