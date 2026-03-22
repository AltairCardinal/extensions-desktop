package android.net

class Uri private constructor(private val uriString: String) {
    fun getScheme(): String? = uriString.substringBefore("://", "").takeIf { it.isNotEmpty() }
    fun getHost(): String? = uriString.substringAfter("://").substringBefore("/").takeIf { it.isNotEmpty() }
    fun getPath(): String? = "/" + uriString.substringAfter("://").substringAfter("/")
    override fun toString(): String = uriString

    companion object {
        @JvmStatic fun parse(uriString: String): Uri = Uri(uriString)
        @JvmStatic fun fromParts(scheme: String, ssp: String, fragment: String?): Uri =
            Uri("$scheme:$ssp${if (fragment != null) "#$fragment" else ""}")
    }
}
