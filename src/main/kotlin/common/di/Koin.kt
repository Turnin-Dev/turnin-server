package com.turnin.common.di

import com.turnin.common.db.databaseModule
import com.turnin.common.firebase.firebaseModule
import com.turnin.common.jwt.di.jwtModule
import com.turnin.common.ml.embeddingModule
import com.turnin.domain.account.di.accountModule
import com.turnin.domain.auth.di.authModule
import com.turnin.domain.block.di.blockModule
import com.turnin.domain.discover.di.discoverModule
import com.turnin.domain.feed.di.feedModule
import com.turnin.domain.file.di.fileModule
import com.turnin.domain.friend.di.friendModule
import com.turnin.domain.keyword.di.keywordModule
import com.turnin.domain.notification.di.notificationModule
import com.turnin.domain.report.di.reportModule
import com.turnin.domain.user.di.userModule
import com.turnin.domain.userKeyword.di.userKeywordModule
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
