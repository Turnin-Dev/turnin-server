package com.peekr.common.di

import com.peekr.common.db.databaseModule
import com.peekr.common.firebase.firebaseModule
import com.peekr.common.jwt.di.jwtModule
import com.peekr.common.ml.embeddingModule
import com.peekr.domain.account.di.accountModule
import com.peekr.domain.auth.di.authModule
import com.peekr.domain.block.di.blockModule
import com.peekr.domain.discover.di.discoverModule
import com.peekr.domain.feed.di.feedModule
import com.peekr.domain.file.di.fileModule
import com.peekr.domain.friend.di.friendModule
import com.peekr.domain.keyword.di.keywordModule
import com.peekr.domain.notification.di.notificationModule
import com.peekr.domain.report.di.reportModule
import com.peekr.domain.user.di.userModule
import com.peekr.domain.userKeyword.di.userKeywordModule
import io.ktor.server.application.Application
import org.koin.core.context.startKoin
import org.koin.ksp.generated.defaultModule
import org.koin.logger.slf4jLogger

/** Koin 설정 */
fun Application.configureKoin() {
//    install(Koin) {
    startKoin {
        slf4jLogger()
        defaultModule()
        modules(
            // Feature
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
            blockModule,
            accountModule,
            notificationModule,
            // 3rd service
            embeddingModule,
            firebaseModule,
            // Util
            coroutineModule,
            databaseModule,
        )
    }
}
