package android.webkit

import android.content.Context

open class WebView(context: Context?) {
    var webViewClient: WebViewClient = WebViewClient()
    val settings: WebSettings = WebSettings()

    fun loadUrl(url: String) {}
    fun loadDataWithBaseURL(baseUrl: String?, data: String, mimeType: String?, encoding: String?, historyUrl: String?) {}
    fun stopLoading() {}
    fun evaluateJavascript(script: String, callback: ((String) -> Unit)?) { callback?.invoke("null") }
    fun destroy() {}
}

open class WebViewClient {
    open fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? = null
    open fun onPageFinished(view: WebView?, url: String?) {}
}

class WebSettings {
    var javaScriptEnabled: Boolean = false
    var domStorageEnabled: Boolean = false
    var databaseEnabled: Boolean = false
    var userAgentString: String = ""
    var blockNetworkImage: Boolean = false
}

open class WebResourceRequest {
    open val url: android.net.Uri? = null
    open val method: String = "GET"
    open val requestHeaders: Map<String, String> = emptyMap()
}

open class WebResourceResponse(
    val mimeType: String? = null,
    val encoding: String? = null,
    val data: java.io.InputStream? = null,
) {
    var statusCode: Int = 200
    var reasonPhrase: String = "OK"
    var responseHeaders: Map<String, String> = emptyMap()
}
