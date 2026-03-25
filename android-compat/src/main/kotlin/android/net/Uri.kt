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
    val encodedQuery: String? get() = query
    val fragment: String? get() = uriString.substringAfter("#", "").takeIf { it.isNotEmpty() }
    val lastPathSegment: String? get() = pathSegments.lastOrNull()
    val encodedPath: String? get() = path
    val authority: String? get() = host

    fun getQueryParameter(key: String): String? =
        query?.split("&")?.firstOrNull { it.startsWith("$key=") }?.substringAfter("=")
            ?.let { java.net.URLDecoder.decode(it, "UTF-8") }

    fun getQueryParameters(key: String): List<String> =
        query?.split("&")?.filter { it.startsWith("$key=") }
            ?.map { java.net.URLDecoder.decode(it.substringAfter("="), "UTF-8") } ?: emptyList()

    fun buildUpon(): Builder = Builder(uriString)

    override fun toString(): String = uriString

    class Builder() {
        private var scheme: String? = null
        private var authority: String? = null
        private var path: String? = null
        private var encodedQueryStr: String? = null
        private val queryParams = mutableListOf<Pair<String, String>>()

        constructor(existingUri: String) : this() {
            val uri = parse(existingUri)
            scheme = uri.scheme
            authority = uri.host
            path = uri.path
        }

        fun scheme(scheme: String) = apply { this.scheme = scheme }
        fun authority(authority: String) = apply { this.authority = authority }
        fun path(path: String) = apply { this.path = path }
        fun appendPath(segment: String) = apply {
            val encoded = java.net.URLEncoder.encode(segment, "UTF-8").replace("+", "%20")
            this.path = (this.path?.trimEnd('/') ?: "") + "/$encoded"
        }
        fun appendEncodedPath(segment: String) = apply {
            this.path = (this.path?.trimEnd('/') ?: "") + "/$segment"
        }
        fun appendQueryParameter(key: String, value: String) = apply {
            encodedQueryStr = null
            queryParams.add(key to value)
        }
        fun encodedQuery(query: String?) = apply {
            encodedQueryStr = query
            queryParams.clear()
        }
        fun clearQuery() = apply { queryParams.clear(); encodedQueryStr = null }
        fun build(): Uri {
            val sb = StringBuilder()
            if (scheme != null) sb.append("$scheme://")
            if (authority != null) sb.append(authority)
            if (path != null) sb.append(path)
            when {
                encodedQueryStr != null -> sb.append("?$encodedQueryStr")
                queryParams.isNotEmpty() -> sb.append("?" + queryParams.joinToString("&") {
                    "${java.net.URLEncoder.encode(it.first, "UTF-8")}=${java.net.URLEncoder.encode(it.second, "UTF-8")}"
                })
            }
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
        @JvmStatic fun withAppendedPath(baseUri: Uri, pathSegment: String): Uri =
            baseUri.buildUpon().appendEncodedPath(pathSegment).build()
    }
}
