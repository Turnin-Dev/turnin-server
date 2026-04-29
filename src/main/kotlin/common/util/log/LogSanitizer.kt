package com.turnin.common.util.log

object LogSanitizer {
    fun sanitize(value: String?): String =
        value
            ?.replace("\t", "\\t")
            ?.replace("\r", "\\r")
            ?.replace("\n", "\\n")
            ?.replace("\"", "\\\"")
            ?: "-"
}
