package android.text

open class Layout {
    open val height: Int = 0
    open val width: Int = 0
    open val lineCount: Int = 0

    enum class Alignment {
        ALIGN_NORMAL, ALIGN_OPPOSITE, ALIGN_CENTER
    }

    open fun getLineTop(line: Int): Int = 0
    open fun getLineBottom(line: Int): Int = 0
    open fun draw(canvas: android.graphics.Canvas) {}

    companion object {
        const val HYPHENATION_FREQUENCY_NONE = 0
        const val HYPHENATION_FREQUENCY_NORMAL = 1
        const val HYPHENATION_FREQUENCY_FULL = 2
    }
}
