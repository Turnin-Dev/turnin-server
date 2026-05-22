package com.turnin.common.ml.keywordCategory

import com.turnin.common.ml.EmbeddingService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.Before
import org.junit.Test

class KeywordCategoryClassifierTest {
    private lateinit var embeddingService: EmbeddingService
    private lateinit var classifier: KeywordCategoryClassifier

    private val vectorA = floatArrayOf(1f, 0f, 0f, 0f)
    private val vectorB = floatArrayOf(0f, 1f, 0f, 0f)

    @Before
    fun setUp() {
        embeddingService = mockk()
        classifier = KeywordCategoryClassifier(embeddingService)

        every { embeddingService.normalize(any()) } answers { firstArg() }
    }

    // -------------------------------------------------------------------------
    // init()
    // -------------------------------------------------------------------------

    @Test
    fun `init 호출 시 카테고리 풀의 모든 키워드를 임베딩한다`() {
        val totalKeywords = KeywordCategoryClassifier.CATEGORY_POOL
            .sumOf { it.keywords.size }

        every { embeddingService.embedAsVector(any()) } returns vectorA

        classifier.init()

        verify(exactly = totalKeywords) { embeddingService.embedAsVector(any()) }
    }

    @Test
    fun `init을 여러 번 호출해도 임베딩은 최초 1회만 수행된다`() {
        every { embeddingService.embedAsVector(any()) } returns vectorA

        classifier.init()
        classifier.init()
        classifier.init()

        val totalKeywords = KeywordCategoryClassifier.CATEGORY_POOL
            .sumOf { it.keywords.size }

        verify(exactly = totalKeywords) { embeddingService.embedAsVector(any()) }
    }

    // -------------------------------------------------------------------------
    // classify() — 초기화 전
    // -------------------------------------------------------------------------

    @Test(expected = IllegalStateException::class)
    fun `init 없이 classify 호출 시 IllegalStateException이 발생한다`() {
        classifier.classify("테스트키워드")
    }

    // -------------------------------------------------------------------------
    // classify() — 초기화 후
    // -------------------------------------------------------------------------

    @Test
    fun `유사도가 THRESHOLD 이상이면 CategoryClassificationResult를 반환한다`() {
        every { embeddingService.embedAsVector(any()) } returns vectorA
        classifier.init()

        every { embeddingService.embedAsVector("키워드") } returns vectorA

        val result = classifier.classify("키워드")

        assertNotNull(result)
        assert(result.similarity >= KeywordCategoryClassifier.THRESHOLD)
    }

    @Test
    fun `유사도가 THRESHOLD 미만이면 null을 반환한다`() {
        every { embeddingService.embedAsVector(any()) } returns vectorA
        classifier.init()

        every { embeddingService.embedAsVector("키워드") } returns vectorB

        val result = classifier.classify("키워드")

        assertNull(result)
    }

    @Test
    fun `유사도가 정확히 THRESHOLD와 같으면 결과를 반환한다`() {
        every { embeddingService.embedAsVector(any()) } returns vectorA
        classifier.init()

        // vectorA(1,0,0,0)와 내적 = 0.5f = THRESHOLD
        val thresholdVector = floatArrayOf(0.5f, 0f, 0f, 0f)
        every { embeddingService.embedAsVector("경계키워드") } returns thresholdVector

        val result = classifier.classify("경계키워드")

        assertNotNull(result)
        assertEquals(KeywordCategoryClassifier.THRESHOLD, result.similarity)
    }

    @Test
    fun `반환된 결과의 similarity는 가장 유사한 카테고리의 값이다`() {
        every { embeddingService.embedAsVector(any()) } returns vectorA
        classifier.init()

        every { embeddingService.embedAsVector("키워드") } returns vectorA

        val result = classifier.classify("키워드")

        assertNotNull(result)
        assertEquals(1.0f, result.similarity)
    }

    // -------------------------------------------------------------------------
    // preprocessKeyword() — classify()를 통한 간접 검증
    // -------------------------------------------------------------------------

    @Test
    fun `뒤에 붙은 숫자가 제거된 키워드로 임베딩이 호출된다`() {
        every { embeddingService.embedAsVector(any()) } returns vectorA
        classifier.init()

        classifier.classify("keyword123")

        verify { embeddingService.embedAsVector("keyword") }
        verify(exactly = 0) { embeddingService.embedAsVector("keyword123") }
    }

    @Test
    fun `전처리 후 빈 문자열이 되면 원본 키워드로 임베딩이 호출된다`() {
        every { embeddingService.embedAsVector(any()) } returns vectorA
        classifier.init()

        classifier.classify("123")

        verify { embeddingService.embedAsVector("123") }
    }

    @Test
    fun `숫자가 없는 키워드는 그대로 임베딩이 호출된다`() {
        every { embeddingService.embedAsVector(any()) } returns vectorA
        classifier.init()

        classifier.classify("일반키워드")

        verify { embeddingService.embedAsVector("일반키워드") }
    }

    @Test
    fun `뒤 숫자만 제거되고 중간 숫자는 유지된다`() {
        every { embeddingService.embedAsVector(any()) } returns vectorA
        classifier.init()

        classifier.classify("key2word3")

        verify { embeddingService.embedAsVector("key2word") }
    }
}
