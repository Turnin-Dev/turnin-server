package com.turnin.common.db

import com.turnin.util.db.TestDatabaseFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.junit.After
import org.junit.Before

class SuspendTransactionTest {
    @Before
    fun setup() {
        TestDatabaseFactory.init()
    }

    @After
    fun teardown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `트랜잭션 재사용 확인`() = runTest {
        val transactionIds = mutableListOf<String>()

        suspendTransaction {
            val outerTxId = TransactionManager.current().id
            transactionIds.add(outerTxId)

            // 중첩 호출
            suspendTransaction {
                val innerTxId = TransactionManager.current().id
                transactionIds.add(innerTxId)
            }
            suspendTransaction {
                val innerTxId = TransactionManager.current().id
                transactionIds.add(innerTxId)
            }
        }

        assertEquals(1, transactionIds.toSet().size)
    }
}
