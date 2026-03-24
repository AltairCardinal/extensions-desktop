package android.os

open class Handler {
    constructor()
    constructor(looper: Looper)

    fun post(r: Runnable): Boolean { r.run(); return true }
    fun postDelayed(r: Runnable, delayMillis: Long): Boolean { r.run(); return true }
    fun removeCallbacks(r: Runnable) {}
    fun removeCallbacksAndMessages(token: Any?) {}
}
