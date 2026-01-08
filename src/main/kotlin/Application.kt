package com.peekr

import com.peekr.ApplicationUtils.initDatabase
import com.peekr.ApplicationUtils.printSection
import com.peekr.ApplicationUtils.printServerSettings
import com.peekr.common.di.configureKoin
import com.peekr.common.exception.configureExceptionHandler
import com.peekr.common.jwt.configureJwtSecurity
import com.peekr.common.ml.EmbeddingService
import com.peekr.common.plugin.configureAPIDocuments
import com.peekr.common.plugin.configureCallLogging
import com.peekr.common.plugin.configureContentNegotiation
import com.peekr.common.plugin.configureCors
import com.peekr.common.plugin.configureResources
import com.peekr.common.plugin.configureRouting
import com.peekr.common.util.AppLoggerFactory
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureKoin()

    // ------------------------------ Initialize ------------------------------
    initDatabase()

    // ------------------------------ Plugins ------------------------------
    configureResources()
    configureCors()

    configureContentNegotiation()
    configureExceptionHandler()
    configureCallLogging()

    configureJwtSecurity()

    configureAPIDocuments()
    configureRouting()

    // ------------------------------ Print ------------------------------
    printSection {
        printServerSettings()
    }

    // ------------------------------ Embedding Service ------------------------------
    EmbeddingService.init(
        onnxModelPath = "src/main/resources/ml/model_int8.onnx",
        tokenizerPath = "src/main/resources/ml/tokenizer.json",
    )

    // ------------------------------ Clean up ------------------------------
    monitor.subscribe(ApplicationStopped) {
        EmbeddingService.close()
        LOGGER.info("Embedding service resources have been safely released.")
    }
}

private val LOGGER = AppLoggerFactory.createLogger("Application")
