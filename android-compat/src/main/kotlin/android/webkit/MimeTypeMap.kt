package android.webkit

object MimeTypeMap {
    private val extToMime = mapOf(
        "jpg" to "image/jpeg", "jpeg" to "image/jpeg",
        "png" to "image/png", "gif" to "image/gif",
        "webp" to "image/webp", "avif" to "image/avif",
        "mp4" to "video/mp4", "webm" to "video/webm",
        "pdf" to "application/pdf",
    )

    @JvmStatic fun getSingleton(): MimeTypeMap = this
    fun getMimeTypeFromExtension(extension: String): String? = extToMime[extension.lowercase()]
    fun getExtensionFromMimeType(mimeType: String): String? = extToMime.entries.find { it.value == mimeType }?.key
}
