package android.webkit

/** JVM stub for android.webkit.URLUtil. */
object URLUtil {
    @JvmStatic
    fun isValidUrl(url: String?): Boolean {
        if (url.isNullOrEmpty()) return false
        return url.startsWith("http://") || url.startsWith("https://") ||
            url.startsWith("ftp://") || url.startsWith("about:") || url.startsWith("data:")
    }

    @JvmStatic
    fun isNetworkUrl(url: String?): Boolean = !url.isNullOrEmpty() &&
        (url.startsWith("http://") || url.startsWith("https://"))

    @JvmStatic
    fun isHttpUrl(url: String?): Boolean = !url.isNullOrEmpty() && url.startsWith("http://")

    @JvmStatic
    fun isHttpsUrl(url: String?): Boolean = !url.isNullOrEmpty() && url.startsWith("https://")

    @JvmStatic
    fun guessUrl(inUrl: String): String {
        if (inUrl.startsWith("http://") || inUrl.startsWith("https://")) return inUrl
        return "http://$inUrl"
    }

    @JvmStatic
    fun guessFileName(url: String?, contentDisposition: String?, mimeType: String?): String {
        return url?.substringAfterLast('/')?.substringBefore('?') ?: "download"
    }
}
