package android.graphics

import java.awt.image.BufferedImage
import java.io.InputStream
import java.io.OutputStream
import javax.imageio.ImageIO

/** JVM implementation of Android Bitmap backed by java.awt.image.BufferedImage. */
class Bitmap(val image: BufferedImage) {
    val width: Int get() = image.width
    val height: Int get() = image.height

    enum class CompressFormat { JPEG, PNG, WEBP, WEBP_LOSSY, WEBP_LOSSLESS }
    enum class Config { ARGB_8888, RGB_565, ALPHA_8, RGBA_F16 }

    val config: Config = Config.ARGB_8888

    fun getPixel(x: Int, y: Int): Int = image.getRGB(x, y)
    fun setPixel(x: Int, y: Int, color: Int) { image.setRGB(x, y, color) }
    fun recycle() {}
    val isRecycled: Boolean = false

    fun eraseColor(color: Int) {
        val g = image.createGraphics()
        g.color = java.awt.Color(color, true)
        g.fillRect(0, 0, width, height)
        g.dispose()
    }

    fun compress(format: CompressFormat, quality: Int, stream: OutputStream): Boolean {
        val fmt = when (format) {
            CompressFormat.JPEG -> "jpg"
            CompressFormat.PNG -> "png"
            else -> "jpg"
        }
        return ImageIO.write(image, fmt, stream)
    }

    companion object {
        @JvmStatic
        fun createBitmap(width: Int, height: Int, config: Config): Bitmap =
            Bitmap(BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB))

        @JvmStatic
        fun createBitmap(source: Bitmap, x: Int, y: Int, width: Int, height: Int): Bitmap {
            val sub = source.image.getSubimage(x, y, width, height)
            val copy = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            copy.createGraphics().apply { drawImage(sub, 0, 0, null); dispose() }
            return Bitmap(copy)
        }
    }
}

object BitmapFactory {
    @JvmStatic
    fun decodeStream(stream: InputStream?): Bitmap? {
        if (stream == null) return null
        return Bitmap(ImageIO.read(stream) ?: BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB))
    }

    @JvmStatic
    fun decodeStream(stream: InputStream?, outPadding: Rect?, opts: Options?): Bitmap? =
        decodeStream(stream)

    @JvmStatic
    fun decodeByteArray(data: ByteArray, offset: Int, length: Int): Bitmap =
        decodeStream(java.io.ByteArrayInputStream(data, offset, length))

    class Options {
        var inJustDecodeBounds: Boolean = false
        var outWidth: Int = 0
        var outHeight: Int = 0
    }
}

class Canvas(val bitmap: Bitmap) {
    private val g2d = bitmap.image.createGraphics()
    var width: Int = bitmap.width
    var height: Int = bitmap.height

    fun drawBitmap(src: Bitmap, srcRect: Rect?, dstRect: Rect, paint: Paint?) {
        val sx = srcRect?.left ?: 0
        val sy = srcRect?.top ?: 0
        val sw = (srcRect?.width() ?: src.width)
        val sh = (srcRect?.height() ?: src.height)
        g2d.drawImage(src.image, dstRect.left, dstRect.top, dstRect.right, dstRect.bottom, sx, sy, sx + sw, sy + sh, null)
    }

    fun drawBitmap(src: Bitmap, left: Float, top: Float, paint: Paint?) {
        g2d.drawImage(src.image, left.toInt(), top.toInt(), null)
    }

    fun drawColor(color: Int) {
        g2d.color = java.awt.Color(color, true)
        g2d.fillRect(0, 0, width, height)
    }

    fun save(): Int = 0
    fun restore() {}
    fun translate(dx: Float, dy: Float) { g2d.translate(dx.toDouble(), dy.toDouble()) }
}

open class Paint {
    var color: Int = Color.BLACK
    var textSize: Float = 14f
    var isAntiAlias: Boolean = false
    var style: Style = Style.FILL
    var strokeWidth: Float = 0f
    var typeface: Typeface? = null

    enum class Style { FILL, STROKE, FILL_AND_STROKE }

    fun measureText(text: String): Float = text.length * textSize * 0.6f
    fun setTypeface(typeface: Typeface?): Typeface? { this.typeface = typeface; return typeface }
}

class Typeface private constructor() {
    companion object {
        @JvmField val DEFAULT = Typeface()
        @JvmField val DEFAULT_BOLD = Typeface()
        @JvmField val MONOSPACE = Typeface()

        const val NORMAL = 0
        const val BOLD = 1
        const val ITALIC = 2

        @JvmStatic fun create(family: String?, style: Int): Typeface = Typeface()
        @JvmStatic fun create(typeface: Typeface?, style: Int): Typeface = Typeface()
    }
}

class Rect(var left: Int = 0, var top: Int = 0, var right: Int = 0, var bottom: Int = 0) {
    fun width() = right - left
    fun height() = bottom - top
    fun set(l: Int, t: Int, r: Int, b: Int) { left = l; top = t; right = r; bottom = b }
    fun isEmpty() = width() <= 0 || height() <= 0
}

class RectF(var left: Float = 0f, var top: Float = 0f, var right: Float = 0f, var bottom: Float = 0f)

object Color {
    const val BLACK = -0x1000000
    const val WHITE = -0x1
    const val RED = -0x10000
    const val GREEN = -0xFF0100
    const val BLUE = -0xFFFF01
    const val TRANSPARENT = 0

    @JvmStatic fun rgb(r: Int, g: Int, b: Int): Int = -0x1000000 or (r shl 16) or (g shl 8) or b
    @JvmStatic fun argb(a: Int, r: Int, g: Int, b: Int): Int = (a shl 24) or (r shl 16) or (g shl 8) or b
}
