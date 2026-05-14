package com.turnin.util

import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

// ApplicationCleanup 내부 로직 일부만 단위 테스트 진행
class ApplicationCleanupTest {
    @Test
    fun `complete 호출 후 joinAll은 모든 자식 코루틴이 완료될 때까지 대기한다`() = runTest {
        // Given: CompletableJob을 가진 테스트용 스코프 생성
        val parentJob = SupervisorJob()
        val testScope = CoroutineScope(parentJob + Dispatchers.Default)
        val isChildFinished = AtomicBoolean(false)

        // 1. 500ms가 걸리는 자식 코루틴 실행
        testScope.launch {
            delay(500)
            isChildFinished.set(true)
            println("자식 코루틴 작업 완료")
        }

        // When: 종료 로직 실행
        val children = parentJob.children.toList()

        // 새로운 작업 유입 차단
        parentJob.complete()

        // 모든 자식 대기
        children.joinAll()

        // Then: joinAll이 끝난 시점에는 자식 코루틴의 작업이 반드시 완료되어 있어야 함
        assertTrue(isChildFinished.get(), "자식 코루틴이 완료되기 전에 joinAll이 끝났습니다.")
        assertTrue(parentJob.isCompleted, "부모 Job이 완전히 종료되지 않았습니다.")
        println("테스트 통과: 모든 백그라운드 작업이 완료됨")
    }

    @Test
    fun `complete 호출 후에는 새로운 코루틴이 실행되지 않는다`() = runTest {
        val parentJob = SupervisorJob()
        val testScope = CoroutineScope(parentJob)

        // 1. 문을 닫음
        parentJob.complete()

        // 2. 새로운 코루틴 실행 시도
        val newJob = testScope.launch {
            delay(100)
        }

        // Then: 이미 complete 된 Job의 자식으로 추가된 새 Job은 즉시 취소되거나 실행되지 않음
        assertFalse(newJob.isActive)
        assertTrue(newJob.isCancelled)
    }
}
