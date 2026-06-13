package com.turnin.common.model

/**
 * 공지 수신 대상
 */
enum class AnnouncementAudience {
    ALL,
    ADMIN,
    ;

    companion object {
        fun from(role: Role): List<AnnouncementAudience> = when (role) {
            Role.USER -> listOf(ALL)
            Role.ADMIN -> listOf(ALL, ADMIN)
        }
    }
}
