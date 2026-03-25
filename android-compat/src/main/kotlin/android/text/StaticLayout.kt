package android.text

class StaticLayout(
    source: CharSequence,
    paint: TextPaint,
    width: Int,
    align: Layout.Alignment,
    spacingmult: Float,
    spacingadd: Float,
    includepad: Boolean,
) : Layout() {
    private val _width = width
    private val _lineCount: Int
    override val height: Int
    override val width: Int get() = _width
    override val lineCount: Int get() = _lineCount

    init {
        val lineHeight = (paint.textSize * spacingmult + spacingadd).toInt()
        val textWidth = paint.measureText(source.toString())
        _lineCount = maxOf(1, (textWidth / width + 1).toInt())
        height = _lineCount * lineHeight
    }

    override fun draw(canvas: android.graphics.Canvas) {}

    class Builder private constructor(
        private val source: CharSequence,
        private val start: Int,
        private val end: Int,
        private val paint: TextPaint,
        private val width: Int,
    ) {
        private var alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL
        private var includePad: Boolean = true

        fun setAlignment(align: Layout.Alignment) = apply { alignment = align }
        fun setIncludePad(include: Boolean) = apply { includePad = include }
        fun setBreakStrategy(strategy: Int) = apply { /* no-op */ }
        fun setHyphenationFrequency(frequency: Int) = apply { /* no-op */ }
        fun setMaxLines(maxLines: Int) = apply { /* no-op */ }
        fun setLineSpacing(spacingAdd: Float, spacingMult: Float) = apply { /* no-op */ }

        fun build(): StaticLayout = StaticLayout(
            source.subSequence(start, end), paint, width, alignment, 1f, 0f, includePad,
        )

        companion object {
            @JvmStatic
            fun obtain(source: CharSequence, start: Int, end: Int, paint: TextPaint, width: Int): Builder =
                Builder(source, start, end, paint, width)
        }
    }
}
