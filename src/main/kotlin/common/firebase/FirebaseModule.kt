package com.peekr.common.firebase

import com.peekr.common.util.AppDispatchers
import org.koin.dsl.module

val firebaseModule = module {
    single { FcmService(ioDispatcher = AppDispatchers.ioDispatcher) }
}
