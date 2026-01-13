package com.peekr.common.validator

/**
 * presentation 계층에 존재하는 Routes 에서의 입력 유효성 검사를 하고 해당 값을 반환한다.
 *
 * @param label 입력 값 라벨
 */
fun String?.inputValidationAndReturn(label: String = ""): String {
    require(this != null) { "요청 파라미터 '$label'가 필요합니다." }
    require(this.isNotBlank()) { "요청 파라미터 '$label'가 비어있습니다." }
    return this
}

/**
 * presentation 계층에 존재하는 Routes 에서의 입력 유효성 검사를 하고 해당 값을 반환한다.
 *
 * @param label 입력 값 라벨
 */
fun Long?.inputValidationAndReturn(label: String = ""): Long {
    require(this != null) { "요청 파라미터 '$label'가 필요합니다." }
    return this
}

/**
 * presentation 계층에 존재하는 Routes 에서의 입력 유효성 검사를 하고 해당 값을 반환한다.
 *
 * @param label 입력 값 라벨
 */
fun Int?.inputValidationAndReturn(label: String = ""): Int {
    require(this != null) { "요청 파라미터 '$label'가 필요합니다." }
    return this
}

/**
 * presentation 계층에 존재하는 Routes 에서의 입력 유효성 검사를 하고 해당 값을 반환한다.
 *
 * @param label 입력 값 라벨
 */
fun Boolean?.inputValidationAndReturn(label: String = ""): Boolean {
    require(this != null) { "요청 파라미터 '$label'가 필요합니다." }
    return this
}
