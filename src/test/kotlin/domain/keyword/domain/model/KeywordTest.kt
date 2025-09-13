package com.peekr.domain.keyword.domain.model

import com.peekr.common.validator.ValidatorException
import com.peekr.domain.core.model.KeywordId
import com.peekr.domain.core.model.UserId
import kotlin.test.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class KeywordTest {
    @Test
    fun `정상적인 키워드 생성()`() {
        assertDoesNotThrow {
            Keyword(
                id = KeywordId(0),
                keyword = "sample",
                createdBy = UserId(0),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
        }
    }

    @Test
    fun `키워드 길이 제약 위반시 ValidatorException 예외가 발생한다`() {
        assertThrows<ValidatorException> {
            Keyword(
                id = KeywordId(0),
                keyword = "a".repeat(Keyword.MAX_LENGTH + 1),
                createdBy = UserId(0),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
        }
    }

    @Test
    fun `키워드가 비어있을 경우 ValidatorException 예외가 발생한다`() {
        assertThrows<ValidatorException> {
            Keyword(
                id = KeywordId(0),
                keyword = "",
                createdBy = UserId(0),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
        }
    }
}
