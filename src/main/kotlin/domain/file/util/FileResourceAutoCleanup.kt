package com.peekr.domain.file.util

import com.peekr.common.util.AppLoggerFactory
import com.peekr.domain.file.infrastructure.service.impl.CloudflareR2Service
import com.peekr.domain.file.infrastructure.service.impl.S3PresignerFactory
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import org.koin.ktor.ext.get

fun Application.fileResourceAutoCleanup() {
    val s3PresignerFactory = get<S3PresignerFactory>()
    val cloudflareR2Service = get<CloudflareR2Service>()

    monitor.subscribe(ApplicationStopping) {
        closeS3PresignerFactory(s3PresignerFactory, "ApplicationStopping")
        closeCloudflareR2Service(cloudflareR2Service, "ApplicationStopping")
    }
}

private fun closeS3PresignerFactory(s3PresignerFactory: S3PresignerFactory, reason: String) {
    try {
        LOGGER.info("Closing s3PresignerFactory on '$reason'")
        s3PresignerFactory.close()
        LOGGER.info("S3PresignerFactory closed successfully")
    } catch (e: Exception) {
        LOGGER.error(e, "Falied to close s3PresignerFactory")
    }
}

private fun closeCloudflareR2Service(service: CloudflareR2Service, reason: String) {
    try {
        LOGGER.info("Closing CloudflareR2Service on '$reason'")
        service.close()
        LOGGER.info("CloudflareR2Service closed successfully")
    } catch (e: Exception) {
        LOGGER.error(e, "Failed to close CloudflareR2Service")
    }
}

private val LOGGER = AppLoggerFactory.createLogger("fileResourceAutoCleanup")
