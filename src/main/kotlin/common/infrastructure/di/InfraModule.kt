package com.turnin.common.infrastructure.di

import com.turnin.common.infrastructure.cloudflare.CloudflareR2Client
import com.turnin.common.util.log.AppLoggerFactory
import org.koin.dsl.module
import org.koin.dsl.onClose

val infraModule = module {
    single {
        CloudflareR2Client(get())
    } onClose {
        try {
            LOGGER.info("Closing CloudflareR2Client...")
            it?.close()
            LOGGER.info("CloudflareR2Client closed successfully")
        } catch (e: Exception) {
            LOGGER.error(e, "Failed to close CloudflareR2Client")
        }
    }
}

private val LOGGER = AppLoggerFactory.createLogger("InfraModule")
