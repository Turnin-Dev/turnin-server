package com.turnin.domain.file.di

import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.domain.file.application.provider.FileDeletionSupportApi
import com.turnin.domain.file.application.provider.FileProviderApi
import com.turnin.domain.file.application.usecase.DeleteFileUseCase
import com.turnin.domain.file.application.usecase.FileUseCases
import com.turnin.domain.file.application.usecase.GetFileUpdateUrlUseCase
import com.turnin.domain.file.application.usecase.GetFileUploadUrlUseCase
import com.turnin.domain.file.domain.service.FileService
import com.turnin.domain.file.infrastructure.service.impl.CloudflareR2Service
import com.turnin.domain.file.infrastructure.service.impl.FileServiceImpl
import com.turnin.domain.file.infrastructure.service.impl.S3PresignerFactory
import org.koin.dsl.module
import org.koin.dsl.onClose

val fileModule = module {
    // Service
    single<FileService> { FileServiceImpl(get()) }

    // Provider
    single { FileProviderApi(get()) }
    single { FileDeletionSupportApi(get()) }

    // UseCase
    single { GetFileUploadUrlUseCase(get()) }
    single { GetFileUpdateUrlUseCase(get()) }
    single { DeleteFileUseCase(get()) }
    single { FileUseCases(get(), get()) }

    // Third-Party
    single {
        S3PresignerFactory()
    } onClose {
        try {
            LOGGER.info("Closing s3PresignerFactory...")
            it?.close()
            LOGGER.info("S3PresignerFactory closed successfully")
        } catch (e: Exception) {
            LOGGER.error(e, "Failed to close s3PresignerFactory")
        }
    }

    // Infra
    single {
        CloudflareR2Service(get(), get())
    } onClose {
        try {
            LOGGER.info("Closing CloudflareR2Service...")
            it?.close()
            LOGGER.info("CloudflareR2Service closed successfully")
        } catch (e: Exception) {
            LOGGER.error(e, "Failed to close CloudflareR2Service")
        }
    }
}

private val LOGGER = AppLoggerFactory.createLogger("FileModule")
