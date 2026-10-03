package com.turnin.domain.pingPong.di

import com.turnin.domain.pingPong.application.usecase.CreatePingPongAnswerUseCase
import com.turnin.domain.pingPong.application.usecase.CreatePingPongUseCase
import com.turnin.domain.pingPong.application.usecase.GetPingPongsUseCase
import com.turnin.domain.pingPong.application.usecase.PingPongUseCases
import com.turnin.domain.pingPong.domain.provider.BlockProvider
import com.turnin.domain.pingPong.domain.provider.UserKeywordProvider
import com.turnin.domain.pingPong.domain.repository.PingPongRepository
import com.turnin.domain.pingPong.infrastructure.provider.BlockProviderImpl
import com.turnin.domain.pingPong.infrastructure.provider.UserKeywordProviderImpl
import com.turnin.domain.pingPong.infrastructure.repository.PingPongRepositoryImpl
import org.koin.dsl.module

val pingPongModule = module {
    // Repository
    single<PingPongRepository> { PingPongRepositoryImpl() }

    // Provider
    single<UserKeywordProvider> { UserKeywordProviderImpl(get()) }
    single<BlockProvider> { BlockProviderImpl(get()) }

    // UseCases
    single { CreatePingPongUseCase(get(), get()) }
    single { CreatePingPongAnswerUseCase(get(), get(), get()) }
    single { GetPingPongsUseCase(get(), get()) }
    single { PingPongUseCases(get(), get(), get()) }
}
