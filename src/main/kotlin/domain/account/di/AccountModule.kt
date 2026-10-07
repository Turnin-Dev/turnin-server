package com.turnin.domain.account.di

import com.turnin.domain.account.application.AccountUseCases
import com.turnin.domain.account.application.DeleteAccountUseCase
import com.turnin.domain.account.application.HardDeleteExpiredAccountsUseCase
import com.turnin.domain.account.application.HardDeleteExpiredUserKeywordsUseCase
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
            get(),
            get(),
            get(),
        )
    }
    single {
        HardDeleteExpiredAccountsUseCase(
            get(),
            get(),
            get(),
        )
    }
    single {
        HardDeleteExpiredUserKeywordsUseCase(
            get(),
            get(),
        )
    }
    single {
        AccountUseCases(get())
    }
}
