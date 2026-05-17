package com.turnin.common.ml.keywordCategory

enum class KeywordCategory(
    val displayName: String,
    val id: Int,
) {
    FOOD("음식", 1),
    FASHION("패션", 2),
    BEAUTY("뷰티", 3),
    TRAVEL("여행", 4),
    FITNESS("운동", 5),
    DAILY("일상", 6),
    ROMANCE("연애", 7),
    CAREER("직장", 8),
    TECH("기술", 9),
    ART("예술", 10),
    ENTERTAINMENT("엔터테인먼트", 11),
    SELF_DEVELOPMENT("자기계발", 12),
    EMOTION("감정", 13),
    ;

    companion object {
        fun fromId(id: Int): KeywordCategory? = entries.find { it.id == id }
    }
}
