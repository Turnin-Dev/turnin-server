package com.turnin.domain.feed.application.dto

import java.util.Base64
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 피드 조회에 필요한 커서
 *
 * @property v 피드 커서 버전
 * @property seed 피드 시드
 * @property sessionMaxId 청크 상한 값. 첫 요청 응답에서 받아 다음 커서에 고정
 * @property windowAnchorId 청크의 시작 기준이 되는 uk_id (이 값보다 작은 글부터 조회, 초기 조회 시 null)
 * @property lastShuffleKey 다음 페이지 커서
 * @property lastUkId 다음 페이지 커서
 */
@Serializable
data class FeedCursor(
    val v: Int = 1,
    val seed: String,
    val sessionMaxId: Long? = null,
    val windowAnchorId: Long? = null,
    val lastShuffleKey: Int? = null,
    val lastUkId: Long? = null,
) {
    companion object {
        fun initial(): FeedCursor = FeedCursor(
            seed = FeedCursorCodec.newSeed(),
            windowAnchorId = null,
        )
    }
}

/**
 * 피드 커서 코덱
 *
 * 시드 생성 / 인코딩 / 디코딩을 지원한다.
 */
object FeedCursorCodec {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun encode(cursor: FeedCursor): String =
        Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(json.encodeToString(cursor).toByteArray(Charsets.UTF_8))

    fun decodeOrNull(raw: String?): FeedCursor? {
        if (raw.isNullOrBlank()) return null
        return try {
            val decoded = Base64.getUrlDecoder().decode(raw).toString(Charsets.UTF_8)
            json.decodeFromString<FeedCursor>(decoded)
        } catch (e: Exception) {
            null
        }
    }

    fun newSeed(): String = UUID.randomUUID().toString().take(12)
}
