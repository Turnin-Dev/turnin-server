package com.turnin.common.ml.keywordCategory

import com.turnin.common.ml.EmbeddingService
import com.turnin.common.util.log.AppLoggerFactory
import kotlin.collections.maxByOrNull

/**
 * 카테고리 분류 결과
 *
 * @property category 키워드 카테고리
 * @property similarity 키워드 <-> 카테고리 유사도
 */
data class CategoryClassificationResult(
    val category: KeywordCategory,
    val similarity: Float,
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

        val CATEGORY_POOL = listOf(
            CategoryPool(KeywordCategory.FOOD, listOf("음식", "맛집", "요리", "카페", "레스토랑", "식당", "먹거리", "brunch", "foodie")),
            CategoryPool(
                KeywordCategory.FASHION,
                listOf("패션", "옷", "스타일", "코디", "데일리룩", "옷차림", "트렌드", "OOTD", "fashion", "style"),
            ),
            CategoryPool(
                KeywordCategory.BEAUTY,
                listOf("뷰티", "메이크업", "화장", "스킨케어", "미용", "뷰티루틴", "beauty", "makeup", "skincare"),
            ),
            CategoryPool(
                KeywordCategory.TRAVEL,
                listOf("여행", "국내여행", "해외여행", "관광", "숙박", "호텔", "tour", "travel", "hotel"),
            ),
            CategoryPool(
                KeywordCategory.FITNESS,
                listOf("운동", "스포츠", "헬스", "피트니스", "구기종목", "야외활동", "홈트", "fitness", "sports"),
            ),
            CategoryPool(KeywordCategory.DAILY, listOf("일상", "브이로그", "하루", "소소한", "일상기록", "vlog", "daily")),
            CategoryPool(
                KeywordCategory.ROMANCE,
                listOf("연애", "썸", "짝사랑", "사랑", "이별", "데이트", "romance", "relationship"),
            ),
            CategoryPool(KeywordCategory.CAREER, listOf("직장", "커리어", "취업", "이직", "업무", "회사생활", "career", "work")),
            CategoryPool(KeywordCategory.TECH, listOf("기술", "IT", "개발", "프로그래밍", "소프트웨어", "디지털", "tech", "software")),
            CategoryPool(KeywordCategory.ART, listOf("예술", "창작", "그림", "사진", "음악", "디자인", "영상제작", "art", "creative")),
            CategoryPool(
                KeywordCategory.ENTERTAINMENT,
                listOf("엔터테인먼트", "드라마", "영화", "아이돌", "콘서트", "유튜브", "게임", "entertainment"),
            ),
            CategoryPool(
                KeywordCategory.SELF_DEVELOPMENT,
                listOf("자기계발", "독서", "명상", "습관", "성장", "공부", "동기부여", "self-improvement"),
            ),
            CategoryPool(
                KeywordCategory.EMOTION,
                listOf("감정", "힐링", "기분", "심리", "불안", "우울", "외로움", "행복", "분노", "공허함", "설렘", "emotion", "feeling"),
            ),
        )
    }

    /**
     * 카테고리 풀
     *
     * @property category 키워드 카테고리
     * @property keywords 카테고리 키워드 목록
     */
    data class CategoryPool(
        val category: KeywordCategory,
        val keywords: List<String>,
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
            pool.category to avgVector
        }
        initialized = true
        LOGGER.info("KeywordCategoryClassifier initialized with ${categoryVectors.size} categories")
    }

    /**
     * 키워드를 분류하여 카테고리와 유사도를 반환한다.
     * 유사도가 THRESHOLD 미만이면 `null`을 반환한다. (미분류)
     *
     * @param keyword 분류할 키워드
     * @return [CategoryClassificationResult] or `null`(미분류)
     */
    fun classify(keyword: String): CategoryClassificationResult? {
        check(initialized) { "KeywordCategoryClassifier is not initialized." }

        val kwVector = embeddingService.embedAsVector(keyword)

        val (category, similarity) = categoryVectors
            .map { (cat, vector) -> cat to cosineSimilarity(kwVector, vector) }
            .maxByOrNull { it.second }
            ?: return null

        return if (similarity >= THRESHOLD) {
            CategoryClassificationResult(category, similarity)
        } else {
            null // 미분류
        }
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
}

private val LOGGER = AppLoggerFactory.createLogger<KeywordCategoryClassifier>()
