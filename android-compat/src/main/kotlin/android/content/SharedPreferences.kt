package android.content

import java.util.prefs.Preferences as JavaPrefs

interface SharedPreferences {
    fun getString(key: String, defValue: String?): String?
    fun getBoolean(key: String, defValue: Boolean): Boolean
    fun getInt(key: String, defValue: Int): Int
    fun getLong(key: String, defValue: Long): Long
    fun getFloat(key: String, defValue: Float): Float
    fun getStringSet(key: String, defValues: Set<String>?): Set<String>?
    fun getAll(): Map<String, *>
    operator fun contains(key: String): Boolean
    fun registerOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {}
    fun unregisterOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {}
    fun edit(): Editor

    interface Editor {
        fun putString(key: String, value: String?): Editor
        fun putBoolean(key: String, value: Boolean): Editor
        fun putInt(key: String, value: Int): Editor
        fun putLong(key: String, value: Long): Editor
        fun putFloat(key: String, value: Float): Editor
        fun putStringSet(key: String, values: Set<String>?): Editor
        fun remove(key: String): Editor
        fun clear(): Editor
        fun commit(): Boolean
        fun apply()
    }

    interface OnSharedPreferenceChangeListener {
        fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?)
    }
}

/** JVM implementation of SharedPreferences backed by java.util.prefs.Preferences. */
class JavaSharedPreferences(node: String) : SharedPreferences {
    private val prefs: JavaPrefs = JavaPrefs.userRoot().node("/mihon-ext/${node.replace(' ', '_')}")

    override fun getString(key: String, defValue: String?): String? = prefs.get(key, defValue)
    override fun getBoolean(key: String, defValue: Boolean): Boolean = prefs.getBoolean(key, defValue)
    override fun getInt(key: String, defValue: Int): Int = prefs.getInt(key, defValue)
    override fun getLong(key: String, defValue: Long): Long = prefs.getLong(key, defValue)
    override fun getFloat(key: String, defValue: Float): Float = prefs.getFloat(key, defValue)
    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
        val raw = prefs.get(key, null) ?: return defValues
        return if (raw.isEmpty()) emptySet() else raw.split("\u001F").toSet()
    }
    override fun getAll(): Map<String, *> = prefs.keys().associateWith { prefs.get(it, null) }
    override operator fun contains(key: String): Boolean = prefs.get(key, null) != null
    override fun edit(): SharedPreferences.Editor = EditorImpl()

    private inner class EditorImpl : SharedPreferences.Editor {
        private val ops = mutableListOf<() -> Unit>()

        override fun putString(key: String, value: String?) = apply { ops += { if (value == null) prefs.remove(key) else prefs.put(key, value) } }
        override fun putBoolean(key: String, value: Boolean) = apply { ops += { prefs.putBoolean(key, value) } }
        override fun putInt(key: String, value: Int) = apply { ops += { prefs.putInt(key, value) } }
        override fun putLong(key: String, value: Long) = apply { ops += { prefs.putLong(key, value) } }
        override fun putFloat(key: String, value: Float) = apply { ops += { prefs.putFloat(key, value) } }
        override fun putStringSet(key: String, values: Set<String>?) = apply {
            ops += { if (values == null) prefs.remove(key) else prefs.put(key, values.joinToString("\u001F")) }
        }
        override fun remove(key: String) = apply { ops += { prefs.remove(key) } }
        override fun clear() = apply { ops += { prefs.clear() } }
        override fun commit(): Boolean { apply(); return true }
        override fun apply() { ops.forEach { it() }; prefs.flush() }
    }
}
