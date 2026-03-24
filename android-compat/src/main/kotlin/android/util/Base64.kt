package android.util

object Base64 {
    const val DEFAULT: Int = 0

    @JvmStatic
    fun decode(input: String, flags: Int): ByteArray {
        return java.util.Base64.getDecoder().decode(input)
    }

    @JvmStatic
    fun encodeToString(input: ByteArray, flags: Int): String {
        return java.util.Base64.getEncoder().encodeToString(input)
    }
}
