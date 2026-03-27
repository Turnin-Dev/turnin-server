package com.peekr.common.ml

import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.config.AppConfig
import org.koin.dsl.module
import org.koin.dsl.onClose

val embeddingModule = module {
    single {
        val appConfig = get<AppConfig>()
        val modelPath = appConfig.getOrDefault(
            "ktor.model.modelPath",
            "src/main/resources/ml/model_int8.onnx",
        )
        val tokenizerPath = appConfig.getOrDefault(
            "ktor.model.tokenizerPath",
            "src/main/resources/ml/tokenizer.json",
        )

        EmbeddingService(
            modelPath = modelPath,
            tokenizerPath = tokenizerPath,
        ).apply { init() }
    } onClose {
        try {
            LOGGER.info("Closing embedding service...")
            it?.close()
            LOGGER.info("Embedding service closed successfully")
        } catch (e: Exception) {
            LOGGER.error(e, "Failed to close embedding service")
        }
    }
}

private val LOGGER = AppLoggerFactory.createLogger("EmbeddingModule")
