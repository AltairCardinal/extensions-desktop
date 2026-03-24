package app.cash.quickjs

/**
 * JVM stub for app.cash.quickjs.QuickJs.
 * Uses javax.script (Nashorn/GraalJS) at runtime, or throws if no engine is available.
 */
class QuickJs private constructor() : java.io.Closeable {
    private val engine: javax.script.ScriptEngine? = run {
        val mgr = javax.script.ScriptEngineManager()
        mgr.getEngineByName("nashorn")
            ?: mgr.getEngineByName("graal.js")
            ?: mgr.getEngineByName("js")
    }

    fun evaluate(script: String): Any? {
        val e = engine ?: throw UnsupportedOperationException(
            "No JavaScript engine available on this JVM. " +
                "QuickJs-dependent extensions are not supported on desktop."
        )
        return e.eval(script)
    }

    override fun close() {}

    companion object {
        @JvmStatic
        fun create(): QuickJs = QuickJs()
    }
}
