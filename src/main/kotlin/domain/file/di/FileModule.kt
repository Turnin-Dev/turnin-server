package com.turnin.domain.file.di

import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.domain.file.application.provider.FileDeletionSupportApi
import com.turnin.domain.file.application.provider.FileProviderApi
import com.turnin.domain.file.application.usecase.DeleteFileUseCase
import com.turnin.domain.file.application.usecase.FileUseCases
import com.turnin.domain.file.application.usecase.GetFileUpdateUrlUseCase
import com.turnin.domain.file.application.usecase.GetFileUploadUrlUseCase
import com.turnin.domain.file.domain.service.FileService
import com.turnin.domain.file.infrastructure.service.impl.FileServiceImpl
import com.turnin.domain.file.infrastructure.service.impl.ImageR2Service
import org.koin.dsl.module

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

    // Infra
    single { ImageR2Service(get(), get()) }
}

private val LOGGER = AppLoggerFactory.createLogger("FileModule")
