package com.turnin.common.ml

import java.io.File
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.measureTimedValue
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.jupiter.api.assertThrows

/**
 * EmbeddingService 통합 테스트
 * 실제 모델 파일을 사용하여 동작을 검증한다.
 */
class EmbeddingServiceTest {
    companion object {
        private val ModelPath = File("src/main/resources/ml/model_int8.onnx").absolutePath
        private val TokenizerPath = File("src/main/resources/ml/tokenizer.json").absolutePath

        // ko-sbert-sts 모델의 벡터 차원
        private const val VECTOR_DIMENSION = 768

        private const val HIGH_SIMILARITY_THRESHOLD = 0.7f
        private const val MEDIUM_SIMILARITY_THRESHOLD = 0.6f

        @JvmStatic
        private lateinit var embeddingService: EmbeddingService

        @JvmStatic
        @BeforeClass
        fun setUp() {
            embeddingService = EmbeddingService(
                modelPath = ModelPath,
                tokenizerPath = TokenizerPath,
            )
            embeddingService.init()
        }

        @JvmStatic
        @AfterClass
        fun teardown() {
            embeddingService.close()
        }
    }

    // ------------------------------ 기본 벡터 생성 테스트 ------------------------------
    @Test
    fun `한글 키워드에 대한 벡터를 성공적으로 생성한다`() {
        val keyword = "산책"
        testVectorGeneration(keyword)
    }

    @Test
    fun `영어 키워드에 대한 벡터를 성공적으로 생성한다`() {
        val keyword = "walking"
        testVectorGeneration(keyword)
    }

    @Test
    fun `긴 문장에 대한 벡터를 성공적으로 생성한다`() {
        val keyword = "요새 산책을 한다"
        testVectorGeneration(keyword)
    }

    @Test
    fun `특수문자에 대한 벡터를 성공적으로 생성한다`() {
        val keyword = "헬스\uD83D\uDCAA"
        testVectorGeneration(keyword)
    }

    // ------------------------------ 유사도 측정 ------------------------------
    @Test
    fun `같은 맥락을 가진 두 텍스트는 높은 유사도를 가진다`() {
        val keyword1 = "산책"
        val keyword2 = "요새 산책을 한다."

        testSimilarity(keyword1, keyword2) { similarity ->
            assertTrue(similarity >= HIGH_SIMILARITY_THRESHOLD)
        }
    }

    @Test
    fun `같은 범주 내에 있는 두 텍스트는 중간 유사도를 가진다`() {
        val keyword1 = "헬스"
        val keyword2 = "운동"

        testSimilarity(keyword1, keyword2) { similarity ->
            assertTrue(similarity >= MEDIUM_SIMILARITY_THRESHOLD)
        }
    }

    @Test
    fun `줄임말 관계에 있는 두 텍스트는 높은 유사도를 가진다`() {
        val keyword1 = "두쫀쿠"
        val keyword2 = "두바이 쫀득 쿠키"

        testSimilarity(keyword1, keyword2) { similarity ->
            assertTrue(similarity >= HIGH_SIMILARITY_THRESHOLD)
        }
    }

    @Test
    fun `다른 주제에 있는 두 텍스트는 낮은 유사도를 가진다`() {
        val keyword1 = "헬스"
        val keyword2 = "음악"

        testSimilarity(keyword1, keyword2) { similarity ->
            assertTrue(similarity < MEDIUM_SIMILARITY_THRESHOLD)
        }
    }

    // ------------------------------ 예외 처리 테스트 ------------------------------
    @Test
    fun `빈 문자열 입력 시 TokenizationFailed 예외가 발생한다`() {
        // given
        val emptyText = ""

        // when, then
        assertThrows<EmbeddingServiceException.TokenizationFailed> {
            embeddingService.embed(emptyText)
        }
    }

    @Test
    fun `공백 문자열 입력 시 TokenizationFailed 예외가 발생한다`() {
        // given
        val blankText = "   "

        // when, then
        assertThrows<EmbeddingServiceException.TokenizationFailed> {
            embeddingService.embed(blankText)
        }
    }

    // ------------------------------ 성능 테스트 ------------------------------
    @Test
    fun `단일 텍스트 처리 시간 측정`() {
        testVectorGeneration("산책") { _, durationMillisecond ->
            assertTrue(
                durationMillisecond < 100,
                "단일 키워드 처리는 100ms 이내여야 한다. (실제: $durationMillisecond)",
            )
        }
    }

    // ------------------------------ 일관성 테스트 ------------------------------
    @Test
    fun `동일 입력에는 동일한 벡터 값이 출력되어야 한다`() {
        // given
        val keyword = "산책"

        // when
        val vector1 = embeddingService.embed(keyword)
        val vector2 = embeddingService.embed(keyword)

        // then
        assertEquals(vector1, vector2)
    }

    // ------------------------------ 헬퍼 함수 ------------------------------

    /**
     * 벡터 문자열을 FloatArray로 파싱
     * "[0.1,0.2,0.3]" -> FloatArray(0.1f, 0.2f, 0.3f)
     */
    private fun parseVector(vectorString: String): FloatArray = vectorString
        .removeSurrounding("[", "]")
        .split(",")
        .map { it.trim().toFloat() }
        .toFloatArray()

    /**
     * 코사인 유사도 계산
     * 값: -1.0 ~ 1.0 (1.0에 가까울수록 유사)
     */
    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        require(a.size == b.size) { "벡터 차원이 동일해야 합니다: a=${a.size}, b=${b.size}" }

        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0

        for (i in a.indices) {
            dotProduct += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }

        val denominator = sqrt(normA) * sqrt(normB)
        return if (denominator > 0) {
            (dotProduct / denominator).toFloat()
        } else {
            0f
        }
    }

    /**
     * 코사인 유사도 계산 (정규화된 벡터 기준)
     *
     * 정규화된 벡터 전용 내적 계산 (코사인 유사도와 동일한 결과)
     */
    private fun dotProduct(a: FloatArray, b: FloatArray): Float {
        var result = 0f
        for (i in a.indices) result += a[i] * b[i]
        return result
    }

    // ------------------------------ 공통 테스트 함수 ------------------------------

    /**
     * 텍스트에 대한 벡터 생성 공통 테스트
     *
     * @param text 테스트할 텍스트
     * @param additionalAssertion 부가 검증 람다 (벡터 값과 생성 실행 시간이 파라미터로 주어진다.)
     */
    private fun testVectorGeneration(
        text: String,
        additionalAssertion: (vector: String, durationMillisecond: Long) -> Unit = { _, _ -> },
    ) {
        // when
        val timedValue = measureTimedValue {
            embeddingService.embed(text)
        }
        val vector = timedValue.value
        val dimensions = parseVector(vector)

        println("벡터 생성 실행 시간: ${timedValue.duration.inWholeMilliseconds}ms")

        // then
        assertTrue(vector.startsWith("["))
        assertTrue(vector.endsWith("]"))
        assertEquals(
            VECTOR_DIMENSION,
            dimensions.size,
            "벡터 차원이 ${VECTOR_DIMENSION}이어야 한다.",
        )
        additionalAssertion(vector, timedValue.duration.inWholeMilliseconds)
    }

    /**
     * 두 텍스트에 대한 유사도 공통 테스트
     */
    private fun testSimilarity(
        text1: String,
        text2: String,
        similarityAssertion: (Float) -> Unit,
    ) {
        // when
        val vector1 = parseVector(embeddingService.embed(text1))
        val vector2 = parseVector(embeddingService.embed(text2))
        val similarity = dotProduct(vector1, vector2)

        // then
        similarityAssertion(similarity)
        println("유사도: ${String.format("%.4f", similarity)} ($text1 <-> $text2)")
    }
}
