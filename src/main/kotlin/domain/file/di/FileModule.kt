package com.peekr.domain.file.di

import com.peekr.domain.file.application.provider.FileProviderApi
import com.peekr.domain.file.application.usecase.DeleteFileUseCase
import com.peekr.domain.file.application.usecase.FileUseCases
import com.peekr.domain.file.application.usecase.GetFileUpdateUrlUseCase
import com.peekr.domain.file.application.usecase.GetFileUploadUrlUseCase
import com.peekr.domain.file.domain.service.FileService
import com.peekr.domain.file.infrastructure.service.impl.CloudflareR2Service
import com.peekr.domain.file.infrastructure.service.impl.FileServiceImpl
import com.peekr.domain.file.infrastructure.service.impl.S3PresignerFactory
import org.koin.dsl.module

val fileModule = module {
    // Service
    single<FileService> { FileServiceImpl(get()) }

    // Provider
    single { FileProviderApi(get()) }

    // UseCase
    single { GetFileUploadUrlUseCase(get()) }
    single { GetFileUpdateUrlUseCase(get()) }
    single { DeleteFileUseCase(get()) }
    single { FileUseCases(get(), get()) }

    // Third-Party
    single { S3PresignerFactory() }

    // Infra
    single { CloudflareR2Service(get(), get()) }
}
