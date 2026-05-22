package com.turnin.domain.keyword.application.usecase

import com.turnin.common.ml.keywordCategory.KeywordCategoryClassifier
import com.turnin.common.model.KeywordName
import com.turnin.common.model.id.UserId
import com.turnin.domain.keyword.application.dto.KeywordDto
import com.turnin.domain.keyword.application.dto.toDto
import com.turnin.domain.keyword.domain.repository.KeywordRepository
import com.turnin.domain.keyword.exception.KeywordException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 키워드 생성 유스케이스
 *
 * @see invoke
 */
class CreateKeywordUseCase(
    private val keywordRepository: KeywordRepository,
    private val keywordCategoryClassifier: KeywordCategoryClassifier,
) {
    /**
     * 키워드를 생성하고 해당 키워드의 카테고리를 분류한다.
     *
     * 최종적으로 키워드, 임베딩(키워드), 카테고리, 유사도를 저장한다.
     *
     * 추후 미분류 키워드에 대한 폴백 전략이 필요하다.
     *
     * (미분류 키워드끼리만 벡터 연산 등)
     *
     * @param keywordName 키워드명
     * @param createdBy 키워드 최초등록자 ID
     *
     * @return [KeywordDto] 키워드 DTO를 반환한다
     *
     * @throws KeywordException.EmbeddingFailed 임베딩 과정에서 에러 발생 시 예외가 발생한다.
     */
    suspend operator fun invoke(
        keywordName: String,
        createdBy: UserId,
    ): KeywordDto {
        val keywordNameVO = KeywordName(keywordName)

        // 임베딩 + 카테고리 분류를 같은 스레드풀에서 수행
        val classificationResult = withContext(Dispatchers.Default) {
            keywordCategoryClassifier.classify(keywordName)
        }

        return keywordRepository
            .create(
                keywordName = keywordNameVO,
                embeddedKeyword = classificationResult.preprocessedKeywordVector,
                category = classificationResult.category,
                categorySimilarity = classificationResult.similarity,
                createdBy = createdBy,
            ).toDto()
    }
}
