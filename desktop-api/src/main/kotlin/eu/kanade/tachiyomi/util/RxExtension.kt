package eu.kanade.tachiyomi.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rx.Observable

suspend fun <T> Observable<T>.awaitSingle(): T = withContext(Dispatchers.IO) { toBlocking().single() }
