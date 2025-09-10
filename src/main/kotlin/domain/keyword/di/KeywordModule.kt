package com.peekr.domain.keyword.di

import com.peekr.domain.keyword.domain.repository.KeywordRepository
import com.peekr.domain.keyword.infrastructure.repository.impl.KeywordRepositoryImpl
import org.koin.dsl.module

val keywordModule = module {
    // Repository
    single<KeywordRepository> { KeywordRepositoryImpl() }
}
