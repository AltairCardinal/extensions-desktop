package android.util

object Log {
    @JvmStatic fun v(tag: String, msg: String?): Int { println("[V/$tag] $msg"); return 0 }
    @JvmStatic fun d(tag: String, msg: String?): Int { println("[D/$tag] $msg"); return 0 }
    @JvmStatic fun i(tag: String, msg: String?): Int { println("[I/$tag] $msg"); return 0 }
    @JvmStatic fun w(tag: String, msg: String?): Int { System.err.println("[W/$tag] $msg"); return 0 }
    @JvmStatic fun e(tag: String, msg: String?): Int { System.err.println("[E/$tag] $msg"); return 0 }
    @JvmStatic fun e(tag: String, msg: String?, tr: Throwable?): Int { System.err.println("[E/$tag] $msg: $tr"); return 0 }
    @JvmStatic fun w(tag: String, msg: String?, tr: Throwable?): Int { System.err.println("[W/$tag] $msg: $tr"); return 0 }
    @JvmStatic fun wtf(tag: String, msg: String?): Int { System.err.println("[WTF/$tag] $msg"); return 0 }
    @JvmStatic fun wtf(tag: String, msg: String?, tr: Throwable?): Int { System.err.println("[WTF/$tag] $msg: $tr"); return 0 }
    @JvmStatic fun getStackTraceString(tr: Throwable?): String = tr?.stackTraceToString() ?: ""
}
