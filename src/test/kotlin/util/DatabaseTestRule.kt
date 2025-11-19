package com.peekr.util

import kotlin.jvm.Throws
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class DatabaseTestRule : TestRule {
    override fun apply(
        base: Statement?,
        description: Description?,
    ): Statement? = object : Statement() {
        @Throws(Throwable::class)
        override fun evaluate() {
            TestDatabaseFactory.init()

            try {
                base?.evaluate()
            } finally {
                TestDatabaseFactory.cleanUp()
            }
        }
    }
}
