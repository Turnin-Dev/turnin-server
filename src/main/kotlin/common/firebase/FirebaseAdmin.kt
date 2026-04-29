package com.turnin.common.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.turnin.common.util.config.AppConfig
import java.io.File

/**
 * Firebase Admin
 */
object FirebaseAdmin {
    /**
     * Firebase 구성 초기화
     */
    fun initialize(appConfig: AppConfig) {
        // 중복 초기화 방지
        if (FirebaseApp.getApps().isNotEmpty()) return

        val path = appConfig.get("ktor.firebase.serviceAccountPath")
        val serviceAccount = path?.let { File(it).inputStream() }
            ?: throw IllegalStateException("firebase-service-account.json not found in classpath")

        val options = serviceAccount.use { stream ->
            FirebaseOptions
                .builder()
                .setCredentials(GoogleCredentials.fromStream(stream))
                .build()
        }

        FirebaseApp.initializeApp(options)
    }
}
