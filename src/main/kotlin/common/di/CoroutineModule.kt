package com.peekr.common.di

import com.peekr.common.util.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

val BackgroundScopeQualifier = qualifier("backgroundScope")

val coroutineModule = module {
    single<CoroutineScope>(BackgroundScopeQualifier) {
        CoroutineScope(SupervisorJob() + AppDispatchers.ioDispatcher)
    }
}
