package com.turnin.common.util.log

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply

class PrivacyLogFilter : Filter<ILoggingEvent>() {
    override fun decide(event: ILoggingEvent): FilterReply {
        val logType = event.mdcPropertyMap[LogTag.LOG_TYPE.key]
        return if (logType == LogType.PRIVACY.value) FilterReply.ACCEPT else FilterReply.DENY
    }
}

class NormalLogFilter : Filter<ILoggingEvent>() {
    override fun decide(event: ILoggingEvent): FilterReply {
        val logType = event.mdcPropertyMap[LogTag.LOG_TYPE.key]
        return if (logType == LogType.PRIVACY.value) FilterReply.DENY else FilterReply.ACCEPT
    }
}
