package com.peekr.common.util.config

/**
 * 실행 환경 구분
 *
 * @param alias 실행 환경 별칭 (보통 실행 환경에 따라 설정파일을 나누는 경우 파일 이름에서 사용한다.)
 */
enum class RunEnvironment(val alias: String) {
    Dev("dev"),
    Prod("prod"),
}
