package android.util

/** JVM implementation of android.util.LruCache backed by LinkedHashMap. */
open class LruCache<K, V>(private val maxSize: Int) {
    private val map = object : java.util.LinkedHashMap<K, V>(0, 0.75f, true) {
        override fun removeEldestEntry(eldest: Map.Entry<K, V>?) = size > maxSize
    }

    protected open fun create(key: K): V? = null

    @Synchronized
    @Suppress("UNCHECKED_CAST")
    fun get(key: K): V = (map[key] ?: create(key)?.also { put(key, it) }) as V

    @Synchronized
    fun put(key: K, value: V): V? = map.put(key, value)

    @Synchronized
    fun remove(key: K): V? = map.remove(key)

    @Synchronized
    fun size(): Int = map.size

    fun maxSize(): Int = maxSize

    @Synchronized
    fun evictAll() = map.clear()
}
