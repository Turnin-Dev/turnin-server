package com.peekr.domain.keyword.domain.model

import com.peekr.common.model.KeywordId
import com.peekr.common.model.KeywordName
import com.peekr.common.model.UserId
import com.peekr.common.validator.ValidatorException
import kotlin.test.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class KeywordTest {
    @Test
    fun `정상적인 키워드 생성()`() {
        assertDoesNotThrow {
            Keyword(
                id = KeywordId(0),
                name = KeywordName("test"),
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
                name = KeywordName("a".repeat(KeywordName.MAX_LENGTH + 1)),
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
                name = KeywordName(""),
                createdBy = UserId(0),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
        }
    }
}
