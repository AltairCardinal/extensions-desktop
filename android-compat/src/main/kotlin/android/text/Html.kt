package android.text

/**
 * JVM stub for android.text.Html.
 * fromHtml strips tags and decodes basic HTML entities — sufficient for text-to-image rendering.
 */
object Html {
    const val FROM_HTML_MODE_LEGACY = 0

    @JvmStatic
    fun fromHtml(source: String, flags: Int = FROM_HTML_MODE_LEGACY): CharSequence {
        // Strip tags then decode common HTML entities
        return source
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<p[^>]*>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<[^>]+>"), "")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&nbsp;", " ")
            .trim()
    }
}
