package com.peekr.domain.discover.util

object TestVectorFixture {
    // 768차원의 단위 벡터 (모든 요소가 0이고 첫 번째 요소만 1)
    fun unitVector(firstValue: Float = 1.0f): FloatArray = FloatArray(768) { 0f }.apply { this[0] = firstValue }

    // 완전히 다른 벡터 (유사도가 0에 가깝게)
    fun orthogonalVector(): FloatArray = FloatArray(768) { 0f }.apply { this[767] = 1.0f }

    // 기존 벡터 값을 Postgres 벡터 형식에 맞게 문자열로 반환
    fun FloatArray.toPgVectorString(): String =
        this.joinToString(separator = ",", prefix = "[", postfix = "]")
}
