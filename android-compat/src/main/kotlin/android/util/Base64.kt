package android.util

object Base64 {
    const val DEFAULT: Int = 0
    const val NO_PADDING: Int = 1
    const val NO_WRAP: Int = 2
    const val CRLF: Int = 4
    const val URL_SAFE: Int = 8
    const val NO_CLOSE: Int = 16

    @JvmStatic
    fun decode(input: String, flags: Int): ByteArray {
        val cleaned = input.replace("\n", "").replace("\r", "")
        val decoder = if (flags and URL_SAFE != 0) {
            java.util.Base64.getUrlDecoder()
        } else {
            java.util.Base64.getDecoder()
        }
        return decoder.decode(cleaned)
    }

    @JvmStatic
    fun decode(input: ByteArray, flags: Int): ByteArray {
        return decode(String(input, Charsets.UTF_8), flags)
    }

    @JvmStatic
    fun encode(input: ByteArray, flags: Int): ByteArray {
        return encodeToString(input, flags).toByteArray(Charsets.UTF_8)
    }

    @JvmStatic
    fun encodeToString(input: ByteArray, flags: Int): String {
        val encoder = if (flags and URL_SAFE != 0) {
            java.util.Base64.getUrlEncoder()
        } else {
            java.util.Base64.getEncoder()
        }
        val withPadding = if (flags and NO_PADDING != 0) encoder.withoutPadding() else encoder
        val result = withPadding.encodeToString(input)
        return if (flags and NO_WRAP != 0) result else result
    }
}
