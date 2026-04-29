package com.turnin.common.di

import com.turnin.common.util.AppDispatchers
import com.turnin.common.util.log.AppLoggerFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

val ApplicationScopeQualifier = qualifier("applicationScope")

val coroutineModule = module {
    single<CoroutineScope>(ApplicationScopeQualifier) {
        val logger = AppLoggerFactory.createLogger("ApplicationScope")
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            logger.error(throwable, "ApplicationScope에서 처리되지 않은 예외 발생")
        }

        CoroutineScope(
            SupervisorJob() +
                AppDispatchers.defaultDispatcher +
                CoroutineName("application-scope") +
                exceptionHandler,
        )
    }
}
