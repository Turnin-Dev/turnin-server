package com.peekr.common.di

import com.peekr.common.jwt.di.jwtModule
import com.peekr.common.ml.embeddingModule
import com.peekr.domain.auth.di.authModule
import com.peekr.domain.discover.di.discoverModule
import com.peekr.domain.feed.di.feedModule
import com.peekr.domain.file.di.fileModule
import com.peekr.domain.friend.di.friendModule
import com.peekr.domain.keyword.di.keywordModule
import com.peekr.domain.report.di.reportModule
import com.peekr.domain.user.di.userModule
import com.peekr.domain.userKeyword.di.userKeywordModule
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.ksp.generated.defaultModule
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

/** Koin 설정 */
fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        defaultModule()
        modules(
            jwtModule,
            authModule,
            userModule,
            fileModule,
            keywordModule,
            userKeywordModule,
            reportModule,
            friendModule,
            discoverModule,
            feedModule,
            // 3rd service
            embeddingModule,
        )
    }
}
