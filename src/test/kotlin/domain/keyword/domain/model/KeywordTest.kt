package com.peekr.domain.keyword.domain.model

import com.peekr.common.model.KeywordName
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.validator.ValidatorException
import kotlin.test.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class KeywordTest {
    @Test
    fun `정상적인 키워드 생성()`() {
        assertDoesNotThrow {
            Keyword(
                id = KeywordId(1),
                name = KeywordName("test"),
                embedding = "[0,1,0]",
                createdBy = UserId(1),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
        }
    }

    @Test
    fun `키워드 길이 제약 위반시 ValidatorException 예외가 발생한다`() {
        assertThrows<ValidatorException> {
            Keyword(
                id = KeywordId(1),
                name = KeywordName("a".repeat(KeywordName.MAX_LENGTH + 1)),
                embedding = "[0,1,0]",
                createdBy = UserId(1),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
        }
    }

    @Test
    fun `키워드가 비어있을 경우 ValidatorException 예외가 발생한다`() {
        assertThrows<ValidatorException> {
            Keyword(
                id = KeywordId(1),
                name = KeywordName(""),
                embedding = "[0,1,0]",
                createdBy = UserId(1),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
        }
    }
}
