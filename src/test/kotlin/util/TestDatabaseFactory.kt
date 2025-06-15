package com.peekr.util

import com.peekr.domain.auth.infrastructure.persistence.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/** 테스트에서 사용할 H2 DB */
object TestDatabaseFactory {
    fun init() {
        Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
            driver = "org.h2.Driver",
        )
        transaction {
            SchemaUtils.create(Users) // 실제 테이블 모델 그대로 사용
        }
    }
}
