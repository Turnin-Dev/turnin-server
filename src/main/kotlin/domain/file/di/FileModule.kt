package com.peekr.domain.file.di

import com.peekr.domain.file.application.usecase.FileUseCase
import com.peekr.domain.file.application.usecase.FileUseCaseImpl
import com.peekr.domain.file.domain.service.FileService
import com.peekr.domain.file.infrastructure.service.impl.FileServiceImpl
import org.koin.dsl.module

val fileModule = module {
    // Repository
    single<FileService> { FileServiceImpl() }

    // UseCase
    single<FileUseCase> { FileUseCaseImpl(get()) }
}
