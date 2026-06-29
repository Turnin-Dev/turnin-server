package com.turnin.common.di

import com.turnin.common.util.AppDispatchers
import com.turnin.common.util.log.AppLoggerFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

val DefaultApplicationScopeQualifier = qualifier("defaultApplicationScope")
val IOApplicationScopeQualifier = qualifier("ioApplicationScope")

val coroutineModule = module {
    single<CoroutineScope>(DefaultApplicationScopeQualifier) {
        val logger = AppLoggerFactory.createLogger("ApplicationScope")
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            logger.error(throwable, "DefaultApplicationScope에서 처리되지 않은 예외 발생")
        }

        CoroutineScope(
            SupervisorJob() +
                AppDispatchers.defaultDispatcher +
                CoroutineName("default-application-scope") +
                exceptionHandler,
        )
    }

    single<CoroutineScope>(IOApplicationScopeQualifier) {
        val logger = AppLoggerFactory.createLogger("ApplicationScope")
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            logger.error(throwable, "IOApplicationScope에서 처리되지 않은 예외 발생")
        }

        CoroutineScope(
            SupervisorJob() +
                AppDispatchers.ioDispatcher +
                CoroutineName("io-application-scope") +
                exceptionHandler,
        )
    }
}
