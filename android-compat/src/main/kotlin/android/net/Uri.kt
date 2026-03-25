package android.net

class Uri private constructor(private val uriString: String) {
    val scheme: String? get() = uriString.substringBefore("://", "").takeIf { it.isNotEmpty() }
    val host: String? get() = uriString.substringAfter("://").substringBefore("/").substringBefore("?").substringBefore("#").takeIf { it.isNotEmpty() }
    val path: String? get() {
        val afterAuthority = uriString.substringAfter("://").substringAfter("/", "")
        return if (afterAuthority.isEmpty()) "/" else "/" + afterAuthority.substringBefore("?").substringBefore("#")
    }
    val pathSegments: MutableList<String> get() {
        val p = path ?: return mutableListOf()
        return p.split("/").filter { it.isNotEmpty() }.toMutableList()
    }
    val query: String? get() = uriString.substringAfter("?", "").substringBefore("#").takeIf { it.isNotEmpty() }
    val fragment: String? get() = uriString.substringAfter("#", "").takeIf { it.isNotEmpty() }
    val lastPathSegment: String? get() = pathSegments.lastOrNull()
    val encodedPath: String? get() = path

    fun getQueryParameter(key: String): String? {
        return query?.split("&")?.firstOrNull { it.startsWith("$key=") }?.substringAfter("=")
    }

    override fun toString(): String = uriString

    class Builder {
        private var scheme: String? = null
        private var authority: String? = null
        private var path: String? = null
        private val queryParams = mutableListOf<Pair<String, String>>()

        fun scheme(scheme: String) = apply { this.scheme = scheme }
        fun authority(authority: String) = apply { this.authority = authority }
        fun path(path: String) = apply { this.path = path }
        fun appendPath(segment: String) = apply { this.path = (this.path ?: "") + "/$segment" }
        fun appendQueryParameter(key: String, value: String) = apply { queryParams.add(key to value) }
        fun build(): Uri {
            val sb = StringBuilder()
            if (scheme != null) sb.append("$scheme://")
            if (authority != null) sb.append(authority)
            if (path != null) sb.append(path)
            if (queryParams.isNotEmpty()) sb.append("?" + queryParams.joinToString("&") { "${it.first}=${it.second}" })
            return Uri(sb.toString())
        }
    }

    companion object {
        @JvmStatic fun parse(uriString: String): Uri = Uri(uriString)
        @JvmStatic fun fromParts(scheme: String, ssp: String, fragment: String?): Uri =
            Uri("$scheme:$ssp${if (fragment != null) "#$fragment" else ""}")
        @JvmStatic fun encode(s: String): String = java.net.URLEncoder.encode(s, "UTF-8").replace("+", "%20")
        @JvmStatic fun encode(s: String, allow: String?): String = encode(s)
        @JvmStatic fun decode(s: String): String = java.net.URLDecoder.decode(s, "UTF-8")
    }
}
