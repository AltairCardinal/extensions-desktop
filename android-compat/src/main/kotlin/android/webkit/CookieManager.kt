package android.webkit

class CookieManager private constructor() {
    private val cookies = linkedMapOf<String, MutableList<String>>()

    fun setCookie(url: String, value: String) {
        cookies.getOrPut(url) { mutableListOf() }.add(value)
    }

    fun getCookie(url: String): String? {
        return cookies[url]?.joinToString("; ")
    }

    fun removeAllCookies(callback: ((Boolean) -> Unit)?) {
        cookies.clear()
        callback?.invoke(true)
    }

    fun flush() {}

    companion object {
        private val instance = CookieManager()

        @JvmStatic
        fun getInstance(): CookieManager = instance
    }
}
