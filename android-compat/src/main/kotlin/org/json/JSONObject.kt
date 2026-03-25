package org.json

/**
 * Minimal org.json stub for desktop extensions.
 * Most extensions use kotlinx.serialization, but some still use org.json directly.
 * This delegates to the org.json library bundled with Android — on JVM we provide a thin wrapper.
 */
class JSONObject(private val data: MutableMap<String, Any?> = mutableMapOf()) {

    constructor(json: String) : this() {
        // Simple JSON parser — handles basic cases used by extensions
        val trimmed = json.trim()
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            parseObject(trimmed.substring(1, trimmed.length - 1))
        }
    }

    private fun parseObject(content: String) {
        // Use kotlinx or manual parsing for simple key-value pairs
        // For now, use a regex-based approach for basic cases
        val pattern = Regex(""""([^"]+)"\s*:\s*("(?:[^"\\]|\\.)*"|-?\d+(?:\.\d+)?|true|false|null|\{[^}]*\}|\[[^\]]*\])""")
        for (match in pattern.findAll(content)) {
            val key = match.groupValues[1]
            val value = match.groupValues[2]
            data[key] = parseValue(value)
        }
    }

    private fun parseValue(value: String): Any? {
        return when {
            value == "null" -> null
            value == "true" -> true
            value == "false" -> false
            value.startsWith("\"") -> value.substring(1, value.length - 1).replace("\\\"", "\"").replace("\\\\", "\\")
            value.contains(".") -> value.toDoubleOrNull() ?: value
            value.startsWith("[") -> JSONArray(value)
            value.startsWith("{") -> JSONObject(value)
            else -> value.toLongOrNull() ?: value.toIntOrNull() ?: value
        }
    }

    fun getString(key: String): String = data[key]?.toString() ?: throw JSONException("No value for $key")
    fun getInt(key: String): Int = (data[key] as? Number)?.toInt() ?: getString(key).toInt()
    fun getLong(key: String): Long = (data[key] as? Number)?.toLong() ?: getString(key).toLong()
    fun getDouble(key: String): Double = (data[key] as? Number)?.toDouble() ?: getString(key).toDouble()
    fun getBoolean(key: String): Boolean = data[key] as? Boolean ?: getString(key).toBoolean()
    fun getJSONObject(key: String): JSONObject = data[key] as? JSONObject ?: throw JSONException("No JSONObject for $key")
    fun getJSONArray(key: String): JSONArray = data[key] as? JSONArray ?: throw JSONException("No JSONArray for $key")

    fun optString(key: String, fallback: String = ""): String = data[key]?.toString() ?: fallback
    fun optInt(key: String, fallback: Int = 0): Int = (data[key] as? Number)?.toInt() ?: fallback
    fun optLong(key: String, fallback: Long = 0L): Long = (data[key] as? Number)?.toLong() ?: fallback
    fun optBoolean(key: String, fallback: Boolean = false): Boolean = data[key] as? Boolean ?: fallback
    fun optJSONObject(key: String): JSONObject? = data[key] as? JSONObject
    fun optJSONArray(key: String): JSONArray? = data[key] as? JSONArray

    fun has(key: String): Boolean = data.containsKey(key)
    fun isNull(key: String): Boolean = !data.containsKey(key) || data[key] == null
    fun keys(): Iterator<String> = data.keys.iterator()
    fun length(): Int = data.size

    operator fun get(key: String): Any? = data[key]
    operator fun set(key: String, value: Any?) { data[key] = value }

    fun put(key: String, value: Any?): JSONObject { data[key] = value; return this }

    override fun toString(): String {
        val entries = data.entries.joinToString(",") { (k, v) ->
            "\"$k\":${valueToString(v)}"
        }
        return "{$entries}"
    }

    fun toString(indent: Int): String = toString()

    private fun valueToString(v: Any?): String = when (v) {
        null -> "null"
        is String -> "\"${v.replace("\\", "\\\\").replace("\"", "\\\"")}\""
        is Boolean, is Number -> v.toString()
        is JSONObject -> v.toString()
        is JSONArray -> v.toString()
        else -> "\"$v\""
    }
}

class JSONArray(private val data: MutableList<Any?> = mutableListOf()) {

    constructor(json: String) : this() {
        val trimmed = json.trim()
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            // Simple element splitting — handles basic cases
            val content = trimmed.substring(1, trimmed.length - 1).trim()
            if (content.isNotEmpty()) {
                // Split by comma, respecting nested structures
                var depth = 0
                var start = 0
                for (i in content.indices) {
                    when (content[i]) {
                        '{', '[' -> depth++
                        '}', ']' -> depth--
                        ',' -> if (depth == 0) {
                            data.add(parseElement(content.substring(start, i).trim()))
                            start = i + 1
                        }
                    }
                }
                data.add(parseElement(content.substring(start).trim()))
            }
        }
    }

    private fun parseElement(value: String): Any? {
        return when {
            value == "null" -> null
            value == "true" -> true
            value == "false" -> false
            value.startsWith("\"") -> value.substring(1, value.length - 1)
            value.startsWith("{") -> JSONObject(value)
            value.startsWith("[") -> JSONArray(value)
            value.contains(".") -> value.toDoubleOrNull() ?: value
            else -> value.toIntOrNull() ?: value.toLongOrNull() ?: value
        }
    }

    fun length(): Int = data.size
    operator fun get(index: Int): Any = data[index] ?: throw JSONException("Value at $index is null")
    operator fun set(index: Int, value: Any?) { if (index < data.size) data[index] = value else data.add(value) }
    fun getString(index: Int): String = data[index]?.toString() ?: throw JSONException("No string at $index")
    fun getInt(index: Int): Int = (data[index] as? Number)?.toInt() ?: getString(index).toInt()
    fun getLong(index: Int): Long = (data[index] as? Number)?.toLong() ?: getString(index).toLong()
    fun getJSONObject(index: Int): JSONObject = data[index] as? JSONObject ?: throw JSONException("No JSONObject at $index")
    fun getJSONArray(index: Int): JSONArray = data[index] as? JSONArray ?: throw JSONException("No JSONArray at $index")
    fun optString(index: Int, fallback: String = ""): String = data.getOrNull(index)?.toString() ?: fallback
    fun optJSONObject(index: Int): JSONObject? = data.getOrNull(index) as? JSONObject

    fun put(value: Any?): JSONArray { data.add(value); return this }

    override fun toString(): String {
        return "[${data.joinToString(",") { valueToString(it) }}]"
    }

    private fun valueToString(v: Any?): String = when (v) {
        null -> "null"
        is String -> "\"$v\""
        is Boolean, is Number -> v.toString()
        is JSONObject -> v.toString()
        is JSONArray -> v.toString()
        else -> "\"$v\""
    }
}

class JSONException(message: String) : RuntimeException(message)
