package com.peekr.common.ml

import com.peekr.common.util.config.AppConfig
import org.koin.dsl.module

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
    }
}
