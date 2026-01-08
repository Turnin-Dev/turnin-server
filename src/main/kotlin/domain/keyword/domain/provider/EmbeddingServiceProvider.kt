package com.peekr.domain.keyword.domain.provider

/**
 * 외부에서 제공되는 임베딩 서비스
 */
interface EmbeddingServiceProvider {
    /**
     * 임베드 수행
     *
     * @param text 임베딩할 텍스트
     *
     * @throws com.peekr.domain.keyword.exception.KeywordException.EmbeddingFailed 임베딩 과정에서 에러 발생 시 예외가 발생한다.
     */
    fun embed(text: String): String
}
