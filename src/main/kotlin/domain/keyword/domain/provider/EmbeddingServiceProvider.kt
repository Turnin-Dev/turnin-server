package com.turnin.domain.keyword.domain.provider

/**
 * 외부에서 제공되는 임베딩 서비스
 */
interface EmbeddingServiceProvider {
    /**
     * 입력받은 텍스트를 ONNX 모델을 통해 추론하여 문자열 형태의 임베딩 벡터를 추출한다.
     *
     * **[kotlinx.coroutines.Dispatchers.Default]에서 실행하는 것을 권장한다.**
     *
     * @param text 임베딩할 텍스트
     *
     * @throws com.turnin.domain.keyword.exception.KeywordException.EmbeddingFailed 임베딩 과정에서 에러 발생 시 예외가 발생한다.
     */
    fun embed(text: String): String
}
