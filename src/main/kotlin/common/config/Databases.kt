package com.peekr.common.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabases() {
    val dotenv = dotenv()

    val dbUrl = dotenv["DB_URL"] ?: "jdbc:postgresql://localhost:5432/defaultdb"
    val dbUser = dotenv["DB_USER"] ?: "defaultuser"
    val dbPassword = dotenv["DB_PASSWORD"] ?: "defaultpassword"

    val hikariConfig =
        HikariConfig().apply {
            jdbcUrl = dbUrl
            driverClassName = "org.postgresql.Driver"
            username = dbUser
            password = dbPassword
            maximumPoolSize = 10
        }

    try {
        val dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)
        environment.log.info("Database connection successfully: $dbUrl")
    } catch (e: Exception) {
        environment.log.error("Database connection failed: ${e.message}")
    }

//    //TODO: 추후 삭제 예정
//    transaction {
//        SchemaUtils.drop(Users) // 데이터베이스 초기화 (개발 중에만 사용)
//        SchemaUtils.create(Users) // Users 테이블 생성
//    }
}
