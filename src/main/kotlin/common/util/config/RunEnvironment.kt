package com.turnin.common.util.config

/**
 * 실행 환경 구분
 *
 * @param alias 실행 환경 별칭 (보통 실행 환경에 따른 설정파일을 찾는 경우 사용한다.)
 */
enum class RunEnvironment(val alias: String) {
    Dev("dev"),
    Prod("prod"), ;

    companion object {
        fun String.toRunEnvironment() = when (this) {
            "dev" -> Dev
            "prod" -> Prod
            else -> Dev
        }
    }
}
