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
    }
}

object BitmapFactory {
    @JvmStatic
    fun decodeStream(stream: InputStream): Bitmap? =
        ImageIO.read(stream)?.let { Bitmap(it) }

    @JvmStatic
    fun decodeStream(stream: InputStream, outPadding: Rect?, opts: Options?): Bitmap? =
        decodeStream(stream)

    class Options {
        var inJustDecodeBounds: Boolean = false
        var outWidth: Int = 0
        var outHeight: Int = 0
    }
}

class Canvas(val bitmap: Bitmap) {
    private val g2d = bitmap.image.createGraphics()

    fun drawBitmap(src: Bitmap, srcRect: Rect?, dstRect: Rect, paint: Paint?) {
        val sx = srcRect?.left ?: 0
        val sy = srcRect?.top ?: 0
        val sw = (srcRect?.width() ?: src.width)
        val sh = (srcRect?.height() ?: src.height)
        g2d.drawImage(src.image, dstRect.left, dstRect.top, dstRect.right, dstRect.bottom, sx, sy, sx + sw, sy + sh, null)
    }
}

class Paint

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
