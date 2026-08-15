package com.turnin.domain.keyword.application.usecase

import com.turnin.common.model.KeywordName
import com.turnin.common.model.id.UserId
import com.turnin.domain.keyword.application.dto.KeywordDto
import com.turnin.domain.keyword.application.dto.toDto
import com.turnin.domain.keyword.domain.provider.EmbeddingServiceProvider
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
    private val embeddingServiceProvider: EmbeddingServiceProvider,
) {
    /**
     * 키워드를 생성한다.
     *
     * DB에는 키워드, 임베딩을 저장한다.
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

        // 전처리 + 임베딩 수행
        val embeddedKeyword = withContext(Dispatchers.Default) {
            embeddingServiceProvider.embed(preprocessKeyword(keywordName))
        }

        return keywordRepository
            .create(
                keywordName = keywordNameVO,
                embeddedKeyword = embeddedKeyword,
                createdBy = createdBy,
            ).toDto()
    }

    // 키워드 전처리
    private fun preprocessKeyword(keyword: String): String {
        val trimmed = keyword
            .trim()
            .replace(Regex("""[^\w가-힣\s]"""), "") // 특수문자 제거
            .replace(Regex("""\s+"""), " ") // 연속 공백 정규화
            .lowercase() // 소문자 통일
            .trimEnd { it.isDigit() } // 뒤 숫자 제거

        return trimmed.ifEmpty { keyword }
    }
}
