package com.turnin.common.batch

import org.koin.dsl.module

val batchModule = module {
    single { LogBackupBatch(get(), get()) }
}
