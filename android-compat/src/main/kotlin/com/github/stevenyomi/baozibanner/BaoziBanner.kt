package com.github.stevenyomi.baozibanner

import okhttp3.Interceptor
import okhttp3.Response

/**
 * JVM stub for com.github.stevenyomi:baozibanner.
 * The real library handles image unscrambling for Baozi Manhua.
 * This stub is a no-op pass-through interceptor so the extension compiles and loads.
 */
class BaoziBanner(var level: Int = NORMAL) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(chain.request())

    companion object {
        const val NORMAL = 1
        const val PREF = "banner_level"
        const val PREF_TITLE = "图片处理级别"
        const val PREF_SUMMARY = "选择图片处理方式（需重启应用生效）"
        val PREF_ENTRIES: Array<String> = arrayOf("不处理", "普通", "高质量")
        val PREF_VALUES: Array<String> = arrayOf("0", "1", "2")
    }
}
