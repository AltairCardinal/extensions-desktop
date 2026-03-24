package android.webkit

class CookieManager private constructor() {
    private val cookies = linkedMapOf<String, MutableList<String>>()

    fun setCookie(url: String, value: String) {
        cookies.getOrPut(url) { mutableListOf() }.add(value)
    }

    companion object {
        private val instance = CookieManager()

        @JvmStatic
        fun getInstance(): CookieManager = instance
    }
}
