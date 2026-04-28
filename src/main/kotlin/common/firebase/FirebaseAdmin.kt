package com.turnin.common.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

/**
 * Firebase Admin
 */
object FirebaseAdmin {
    /**
     * Firebase 구성 초기화
     */
    fun initialize() {
        // 중복 초기화 방지
        if (FirebaseApp.getApps().isNotEmpty()) return

        val serviceAccount =
            this::class.java.classLoader.getResourceAsStream("firebase-service-account.json")
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
