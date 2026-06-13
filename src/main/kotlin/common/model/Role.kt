package com.turnin.common.model

/** 사용자 역할 */
enum class Role {
    USER,
    ADMIN,
    ;

    companion object {
        /** USER provider에서 허용되는 역할 목록 */
        val allowedUserProviderRoles = setOf(USER.name, ADMIN.name)
    }
}
