package com.turnin.common.util.pagination.cursor

import java.util.Base64
import java.util.UUID
import kotlinx.serialization.json.Json

/**
 * 커서 코덱 (대부분의 경우 커서 페이지네이셔에서 사용한다)
 *
 * 시드 생성 / 인코딩 / 디코딩을 지원한다.
 */
object CursorCodec {
    @PublishedApi
    internal val json = Json {
        ignoreUnknownKeys = true
    }

    inline fun <reified T> encode(cursor: T): String =
        Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(json.encodeToString(cursor).toByteArray(Charsets.UTF_8))

    inline fun <reified T> decodeOrNull(raw: String?): T? {
        if (raw.isNullOrBlank()) return null
        return try {
            val decoded = Base64.getUrlDecoder().decode(raw).toString(Charsets.UTF_8)
            json.decodeFromString<T>(decoded)
        } catch (e: Exception) {
            null
        }
    }

    fun newSeed(): String = UUID.randomUUID().toString().take(12)
}
