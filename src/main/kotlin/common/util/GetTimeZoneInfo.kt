package com.turnin.common.util

import io.ktor.server.application.Application
import java.time.Instant
import java.util.TimeZone
import org.jetbrains.exposed.sql.transactions.transaction

data class TimeZoneInfo(
    val appTimeZone: String,
    val appTime: Instant,
    val dbTimeZone: String,
    val dbTime: Instant,
)

fun Application.getTimeZoneInfo(): TimeZoneInfo = transaction {
    val appTimeZone = TimeZone.getDefault().id
    val appTime = Instant.now()
    val dbTimeZone = exec("SHOW timezone") { rs ->
        rs.next()
        rs.getString(1)
    }!!
    val dbTime = exec("SELECT CURRENT_TIMESTAMP") { rs ->
        rs.next()
        rs.getTimestamp(1).toInstant()
    }!!

    TimeZoneInfo(
        appTimeZone = appTimeZone,
        appTime = appTime,
        dbTimeZone = dbTimeZone,
        dbTime = dbTime,
    )
}
