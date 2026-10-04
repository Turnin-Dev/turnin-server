package com.turnin.domain.pingPong.di

import com.turnin.domain.pingPong.application.provider.PingPongContentReportApi
import com.turnin.domain.pingPong.application.usecase.CreatePingPongAnswerUseCase
import com.turnin.domain.pingPong.application.usecase.CreatePingPongQuestionUseCase
import com.turnin.domain.pingPong.application.usecase.DeletePingPongAnswerUseCase
import com.turnin.domain.pingPong.application.usecase.DeletePingPongQuestionUseCase
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
    single { PingPongContentReportApi(get()) }

    // UseCases
    single { CreatePingPongQuestionUseCase(get(), get()) }
    single { CreatePingPongAnswerUseCase(get(), get(), get()) }
    single { GetPingPongsUseCase(get(), get()) }
    single { DeletePingPongQuestionUseCase(get(), get()) }
    single { DeletePingPongAnswerUseCase(get(), get()) }
    single { PingPongUseCases(get(), get(), get(), get(), get()) }
}
