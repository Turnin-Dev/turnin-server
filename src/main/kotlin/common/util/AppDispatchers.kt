package com.peekr.common.util

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers

object AppDispatchers {
    val ioDispatcher: CoroutineContext = Dispatchers.IO
}
