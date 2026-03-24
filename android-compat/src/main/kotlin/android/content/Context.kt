package android.content

open class Context {
    open fun getSharedPreferences(name: String?, mode: Int): android.content.SharedPreferences? = null

    companion object {
        const val MODE_PRIVATE = 0
    }
}
