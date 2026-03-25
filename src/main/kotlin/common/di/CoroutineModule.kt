package com.peekr.common.di

import com.peekr.common.util.AppLoggerFactory
import io.ktor.server.application.Application
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

val ApplicationScopeQualifier = qualifier("applicationScope")

val coroutineModule = module {
    single<CoroutineScope>(ApplicationScopeQualifier) {
        val application = get<Application>()
        val logger = AppLoggerFactory.createLogger("ApplicationScope")

        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            logger.error(throwable, "ApplicationScope에서 처리되지 않은 예외 발생")
        }

        CoroutineScope(
            SupervisorJob(application.coroutineContext[Job]) +
                CoroutineName("application-scope") +
                exceptionHandler,
        )
    }
}
