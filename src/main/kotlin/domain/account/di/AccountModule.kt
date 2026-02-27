package com.peekr.domain.account.di

import com.peekr.domain.account.application.AccountUseCases
import com.peekr.domain.account.application.DeleteAccountUseCase
import org.koin.dsl.module

val accountModule = module {
    // Usecase
    single {
        DeleteAccountUseCase(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
    single {
        AccountUseCases(get())
    }
}
