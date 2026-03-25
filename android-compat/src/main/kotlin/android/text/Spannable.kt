package android.text

/** JVM stubs for android.text.Spannable, SpannableString, Linkify, etc. */

interface Spannable : CharSequence {
    fun setSpan(what: Any?, start: Int, end: Int, flags: Int)
    fun removeSpan(what: Any?)
}

open class SpannableString(private val source: CharSequence) : Spannable {
    override val length: Int get() = source.length
    override fun get(index: Int): Char = source[index]
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = source.subSequence(startIndex, endIndex)
    override fun toString(): String = source.toString()
    override fun setSpan(what: Any?, start: Int, end: Int, flags: Int) {}
    override fun removeSpan(what: Any?) {}
}

class SpannableStringBuilder(private var source: CharSequence = "") : Spannable {
    override val length: Int get() = source.length
    override fun get(index: Int): Char = source[index]
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = source.subSequence(startIndex, endIndex)
    override fun toString(): String = source.toString()
    override fun setSpan(what: Any?, start: Int, end: Int, flags: Int) {}
    override fun removeSpan(what: Any?) {}
    fun append(text: CharSequence): SpannableStringBuilder { source = source.toString() + text; return this }
}

object Linkify {
    const val ALL = 0x0F
    const val WEB_URLS = 0x01
    const val EMAIL_ADDRESSES = 0x02
    const val PHONE_NUMBERS = 0x04

    @JvmStatic
    fun addLinks(text: android.widget.TextView?, mask: Int): Boolean = false

    @JvmStatic
    fun addLinks(text: Spannable?, mask: Int): Boolean = false
}

object TextUtils {
    enum class TruncateAt { START, MIDDLE, END, MARQUEE }

    @JvmStatic
    fun isEmpty(str: CharSequence?): Boolean = str.isNullOrEmpty()

    @JvmStatic
    fun equals(a: CharSequence?, b: CharSequence?): Boolean = a?.toString() == b?.toString()

    @JvmStatic
    fun join(delimiter: CharSequence, tokens: Iterable<*>): String = tokens.joinToString(delimiter)

    @JvmStatic
    fun join(delimiter: CharSequence, tokens: Array<*>): String = tokens.joinToString(delimiter)
}
