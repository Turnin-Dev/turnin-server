package com.peekr.domain.file.di

import com.peekr.domain.file.application.usecase.FileUseCase
import com.peekr.domain.file.application.usecase.FileUseCaseImpl
import com.peekr.domain.file.domain.service.FileService
import com.peekr.domain.file.infrastructure.service.impl.CloudflareR2Service
import com.peekr.domain.file.infrastructure.service.impl.FileServiceImpl
import com.peekr.domain.file.infrastructure.service.impl.S3PresignerFactory
import org.koin.dsl.module

val fileModule = module {
    // Service
    single<FileService> { FileServiceImpl(get()) }

    // UseCase
    single<FileUseCase> { FileUseCaseImpl(get()) }

    // Third-Party
    single { S3PresignerFactory() }

    // Infra
    single { CloudflareR2Service(get(), get()) }
}
