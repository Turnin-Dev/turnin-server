package com.turnin.common.firebase

import com.turnin.common.util.AppDispatchers
import org.koin.dsl.module

val firebaseModule = module {
    single { FcmService(ioDispatcher = AppDispatchers.ioDispatcher) }
}
