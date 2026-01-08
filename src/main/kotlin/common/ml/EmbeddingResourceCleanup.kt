package com.peekr.common.ml

import com.peekr.common.util.AppLoggerFactory
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import org.koin.ktor.ext.get

fun Application.embeddingResourceAutoCleanup() {
    val embeddingService = get<EmbeddingService>()

    monitor.subscribe(ApplicationStopping) {
        closeEmbeddingService(embeddingService, "Application stopping")
    }
}

private fun closeEmbeddingService(service: EmbeddingService, reason: String) {
    try {
        LOGGER.info("Closing embedding service: $reason")
        service.close()
        LOGGER.info("Embedding service closed successfully")
    } catch (e: Exception) {
        LOGGER.error(e, "Failed to close embedding service")
    }
}

private val LOGGER = AppLoggerFactory.createLogger("embeddingResourceAutoCleanup")
