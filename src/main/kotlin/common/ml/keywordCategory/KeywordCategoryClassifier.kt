package com.turnin.common.ml.keywordCategory

import com.turnin.common.ml.EmbeddingService
import com.turnin.common.util.log.AppLoggerFactory
import kotlin.collections.maxByOrNull

/**
 * 카테고리 분류 결과
 *
 * @property category 키워드 카테고리
 * @property similarity 키워드 <-> 카테고리 유사도
 * @property preprocessedKeywordVector 전처리된 키워드 벡터
 */
data class CategoryClassificationResult(
    val category: KeywordCategory?,
    val similarity: Float?,
    val preprocessedKeywordVector: String,
)

/**
 * 키워드 카테고리 분류기
 *
 * 서버 시작 시 카테고리 풀의 평균 벡터를 미리 계산하여 메모리에 보관한다.
 * 키워드 등록 시 해당 키워드의 카테고리와 유사도를 반환한다.
 *
 * @property embeddingService 임베딩 서비스
 */
class KeywordCategoryClassifier(private val embeddingService: EmbeddingService) {
    companion object {
        const val THRESHOLD = 0.5f

        val CATEGORY_POOL = KeywordCategory.entries.map { CategoryPool(it) }
    }

    /**
     * 카테고리 풀
     *
     * @property category 키워드 카테고리
     * @property keywords 카테고리 키워드 목록
     */
    data class CategoryPool(
        val category: KeywordCategory,
        val keywords: List<String> = category.anchorKeywords,
    )

    // 서버 시작 시 계산된 카테고리 평균 벡터 목록
    private var categoryVectors: List<Pair<KeywordCategory, FloatArray>> = emptyList()

    @Volatile
    private var initialized = false

    /**
     * 카테고리 풀의 평균 벡터를 계산하여 메모리에 적재한다.
     * EmbeddingService.init() 이후에 호출해야 한다.
     */
    @Synchronized
    fun init() {
        if (initialized) return

        categoryVectors = CATEGORY_POOL.map { pool ->
            val vectors = pool.keywords.map { embeddingService.embedAsVector(it) }
            val avgVector = meanVectors(vectors)
            val normalizedVector = embeddingService.normalize(avgVector)
            pool.category to normalizedVector
        }
        initialized = true
        LOGGER.info("KeywordCategoryClassifier initialized with ${categoryVectors.size} categories")
    }

    /**
     * 키워드를 분류하여 카테고리와 유사도를 반환한다.
     *
     * 유사도가 THRESHOLD 미만이면 `null`을 반환한다. (미분류)
     *
     * @param keyword 분류할 키워드
     * @return [CategoryClassificationResult] or `null`(미분류)
     */
    fun classify(keyword: String): CategoryClassificationResult {
        check(initialized) { "KeywordCategoryClassifier is not initialized." }

        val preprocessed = preprocessKeyword(keyword)
        val kwVector = embeddingService.embedAsVector(preprocessed)
        val kwVectorString = embeddingService.vectorToString(kwVector)

        // 미분류 케이스(takeIf)에서는 category, similarity 가 null
        val (category, similarity) = categoryVectors
            .map { (cat, vector) -> cat to cosineSimilarity(kwVector, vector) }
            .maxByOrNull { it.second }
            ?.takeIf { it.second >= THRESHOLD }
            ?: return CategoryClassificationResult(null, null, kwVectorString)

        return CategoryClassificationResult(category, similarity, kwVectorString)
    }

    // 정규화된 벡터끼리의 내적 = 코사인 유사도
    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        var dot = 0f
        for (i in a.indices) dot += a[i] * b[i]
        return dot
    }

    private fun meanVectors(vectors: List<FloatArray>): FloatArray {
        val dim = vectors[0].size
        val result = FloatArray(dim)
        for (v in vectors) {
            for (i in 0 until dim) result[i] += v[i]
        }
        for (i in 0 until dim) result[i] /= vectors.size.toFloat()
        return result
    }

    // 전처리
    private fun preprocessKeyword(keyword: String): String {
        // 특수문자 제거, 소문자로 통합, 뒤 숫자 제거
        val trimmed = keyword
            .trim()
            .replace(Regex("""[^\w가-힣\s]"""), "")
            .lowercase()
            .trimEnd { it.isDigit() }
        // 전처리 후 빈 문자열이 되면 원본 반환
        return trimmed.ifEmpty { keyword }
    }
}

private val LOGGER = AppLoggerFactory.createLogger<KeywordCategoryClassifier>()
