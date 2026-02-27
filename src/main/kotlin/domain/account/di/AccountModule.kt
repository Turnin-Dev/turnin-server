package com.peekr.domain.account.di

import com.peekr.domain.account.application.DeleteAccountUseCase
import org.koin.dsl.module

val accountModule = module {
    single {
        DeleteAccountUseCase(
            get(),
            get(),
            get(),
            get(),
        )
    }
}
