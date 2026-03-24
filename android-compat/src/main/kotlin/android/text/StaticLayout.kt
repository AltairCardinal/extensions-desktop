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
}
