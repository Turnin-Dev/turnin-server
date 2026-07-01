package com.turnin.common.batch

import com.turnin.common.util.AppDispatchers
import org.koin.dsl.module

val batchModule = module {
    single { LogBackupBatch(get(), get(), coroutineDispatcher = AppDispatchers.ioDispatcher) }
    single { HardDeleteExpiredAccountsBatch(get(), get()) }
}
