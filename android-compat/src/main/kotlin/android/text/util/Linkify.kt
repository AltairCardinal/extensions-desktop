package android.text.util

object Linkify {
    const val ALL = 15
    const val EMAIL_ADDRESSES = 2
    const val MAP_ADDRESSES = 8
    const val PHONE_NUMBERS = 4
    const val WEB_URLS = 1

    fun addLinks(text: android.widget.TextView, mask: Int): Boolean = false
    fun addLinks(text: android.text.Spannable, mask: Int): Boolean = false
}
