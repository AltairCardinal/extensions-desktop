package androidx.preference

open class Preference(open val context: Any? = null) {
    var key: String = ""
    var title: CharSequence? = null
    var summary: CharSequence? = null
    var isEnabled: Boolean = true
    var isVisible: Boolean = true
    var isPersistent: Boolean = true
    var order: Int = 0

    private var changeListener: OnPreferenceChangeListener? = null
    private var clickListener: OnPreferenceClickListener? = null

    fun setOnPreferenceChangeListener(listener: OnPreferenceChangeListener?) { changeListener = listener }
    fun setOnPreferenceClickListener(listener: OnPreferenceClickListener?) { clickListener = listener }
    fun getOnPreferenceChangeListener(): OnPreferenceChangeListener? = changeListener

    fun notifyChanged() {}
    fun setDefaultValue(defaultValue: Any?) {}

    fun interface OnPreferenceChangeListener {
        fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean
    }
    fun interface OnPreferenceClickListener {
        fun onPreferenceClick(preference: Preference): Boolean
    }
}

open class DialogPreference(context: Any? = null) : Preference(context) {
    var dialogTitle: CharSequence? = null
    var dialogMessage: CharSequence? = null
}

open class TwoStatePreference(context: Any? = null) : Preference(context) {
    var isChecked: Boolean = false
    override fun setDefaultValue(defaultValue: Any?) { isChecked = defaultValue as? Boolean ?: false }
}

class SwitchPreferenceCompat(context: Any? = null) : TwoStatePreference(context)
class SwitchPreference(context: Any? = null) : TwoStatePreference(context)
class CheckBoxPreference(context: Any? = null) : TwoStatePreference(context)

class EditTextPreference(context: Any? = null) : DialogPreference(context) {
    var text: String? = null
    fun getText(): String? = text
    override fun setDefaultValue(defaultValue: Any?) { if (text == null) text = defaultValue as? String ?: "" }
}

class ListPreference(context: Any? = null) : DialogPreference(context) {
    var entries: Array<CharSequence>? = null
    var entryValues: Array<CharSequence>? = null
    var value: String? = null
    fun getValue(): String? = value
    fun getEntries(): Array<CharSequence>? = entries
    fun getEntryValues(): Array<CharSequence>? = entryValues
    fun findIndexOfValue(v: String?): Int = entryValues?.indexOfFirst { it == v } ?: -1
    override fun setDefaultValue(defaultValue: Any?) { if (value == null) value = defaultValue as? String }
}

class MultiSelectListPreference(context: Any? = null) : DialogPreference(context) {
    var entries: Array<CharSequence>? = null
    var entryValues: Array<CharSequence>? = null
    var values: Set<String> = emptySet()
    fun getValues(): Set<String> = values
    fun getEntries(): Array<CharSequence>? = entries
    fun getEntryValues(): Array<CharSequence>? = entryValues
    override fun setDefaultValue(defaultValue: Any?) {
        if (values.isEmpty()) {
            @Suppress("UNCHECKED_CAST")
            values = (defaultValue as? Set<String>) ?: emptySet()
        }
    }
}

class PreferenceCategory(context: Any? = null) : Preference(context) {
    private val _prefs = mutableListOf<Preference>()
    fun addPreference(preference: Preference): Boolean { _prefs.add(preference); return true }
    fun removeAll() { _prefs.clear() }
}

class PreferenceScreen(context: Any? = null) : Preference(context) {
    private val _prefs = mutableListOf<Preference>()
    val preferenceCount: Int get() = _prefs.size
    fun addPreference(preference: Preference): Boolean { _prefs.add(preference); return true }
    fun getPreference(index: Int): Preference = _prefs[index]
    fun removeAll() { _prefs.clear() }
}
