package com.turnin.common.util.config

import com.turnin.common.util.config.RunEnvironment.Companion.toRunEnvironment
import com.typesafe.config.ConfigFactory
import io.ktor.server.config.ApplicationConfig
import io.ktor.util.logging.KtorSimpleLogger
import org.koin.core.annotation.Singleton

/** 애플리케이션 설정 값 관리 클래스 */
@Singleton
class AppConfig {
    private val applicationConfig: ApplicationConfig by lazy { initAppConfig() }

    /**
     * [key]를 통해 설정 파일에서 값을 가져온다.
     *
     * (만약, 값이 존재하지 않는다면 **`null`** 반환)
     *
     * @param key 설정 값의 키
     */
    fun get(key: String): String? = applicationConfig.propertyOrNull(key)?.getString()

    /**
     * [key]를 통해 설정 파일에서 값을 가져오고 만약, 값이 존재하지 않는다면 [default]값을 대신 사용한다.
     *
     * @param key 설정 값의 키
     * @param default null 대신 사용할 기본 값
     */
    fun getOrDefault(key: String, default: String): String = get(key) ?: default

    /**
     * [key]를 통해 설정 파일에서 값을 가져오고 만약, 값이 존재하지 않는다면 에러가 발생한다.
     *
     * @param key 설정 값의 키
     */
    fun getRequired(key: String): String =
        get(key)
            ?.takeIf { it.isNotEmpty() }
            ?: error("Missing required config: $key")

    // 실행 환경에 맞게 ApplicationConfig를 가져온다.
    private fun initAppConfig(): ApplicationConfig {
        LOGGER.info("ApplicationConfigManager initializing...")
        try {
            val currentConfig = ConfigFactory.load().resolve()
            val environment = currentConfig.getString("ktor.environment")
            val configPath = "application-${environment.toRunEnvironment().alias}.conf"
            LOGGER.info("ApplicationConfigManager initializing success!")
            return ApplicationConfig(configPath)
        } catch (e: Exception) {
            LOGGER.error("ApplicationConfigManager initializing failed: ${e.message}")
            throw e
        }
    }
}

private val LOGGER = KtorSimpleLogger("AppConfigManager")
