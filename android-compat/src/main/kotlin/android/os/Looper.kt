package android.os

class Looper private constructor() {
    companion object {
        @JvmStatic
        fun getMainLooper(): Looper = Looper()

        @JvmStatic
        fun myLooper(): Looper? = Looper()

        @JvmStatic
        fun prepare() {}

        @JvmStatic
        fun loop() {}
    }

    fun quit() {}
    fun quitSafely() {}
    val thread: Thread get() = Thread.currentThread()
}
