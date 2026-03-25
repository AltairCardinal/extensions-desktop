package androidx.preference

open class Preference(open val context: android.content.Context = android.content.Context()) {
    var key: String = ""
    var title: CharSequence? = null
    var summary: CharSequence? = null
    @set:JvmName("_setEnabled")
    var isEnabled: Boolean = true
    @set:JvmName("_setVisible")
    var isVisible: Boolean = true
    var isPersistent: Boolean = true
    var order: Int = 0

    private var changeListener: OnPreferenceChangeListener? = null
    private var clickListener: OnPreferenceClickListener? = null

    fun setOnPreferenceChangeListener(listener: OnPreferenceChangeListener?) { changeListener = listener }
    fun setOnPreferenceClickListener(listener: OnPreferenceClickListener?) { clickListener = listener }
    fun getOnPreferenceChangeListener(): OnPreferenceChangeListener? = changeListener

    fun setEnabled(enabled: Boolean) { isEnabled = enabled }
    fun setVisible(visible: Boolean) { isVisible = visible }
    fun notifyChanged() {}
    open fun setDefaultValue(defaultValue: Any?) {}

    fun interface OnPreferenceChangeListener {
        fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean
    }
    fun interface OnPreferenceClickListener {
        fun onPreferenceClick(preference: Preference): Boolean
    }
}

open class DialogPreference(context: android.content.Context = android.content.Context()) : Preference(context) {
    var dialogTitle: CharSequence? = null
    var dialogMessage: CharSequence? = null
}

open class TwoStatePreference(context: android.content.Context = android.content.Context()) : Preference(context) {
    var isChecked: Boolean = false
    var summaryOn: CharSequence? = null
    var summaryOff: CharSequence? = null
    override fun setDefaultValue(defaultValue: Any?) { isChecked = defaultValue as? Boolean ?: false }
}

class SwitchPreferenceCompat(context: android.content.Context = android.content.Context()) : TwoStatePreference(context)
class SwitchPreference(context: android.content.Context = android.content.Context()) : TwoStatePreference(context)
class CheckBoxPreference(context: android.content.Context = android.content.Context()) : TwoStatePreference(context)

class EditTextPreference(context: android.content.Context = android.content.Context()) : DialogPreference(context) {
    var text: String? = null
    private var bindListener: ((EditTextProxy) -> Unit)? = null
    fun setOnBindEditTextListener(listener: ((EditTextProxy) -> Unit)?) { bindListener = listener }
    override fun setDefaultValue(defaultValue: Any?) { if (text == null) text = defaultValue as? String ?: "" }

    /** Minimal proxy for android.widget.EditText so extensions can configure input. */
    class EditTextProxy {
        var inputType: Int = 0
        var error: CharSequence? = null
        val rootView: android.view.View = android.view.View()
        fun addTextChangedListener(watcher: android.text.TextWatcher?) {}
        fun removeTextChangedListener(watcher: android.text.TextWatcher?) {}
    }
}

class ListPreference(context: android.content.Context = android.content.Context()) : DialogPreference(context) {
    var entries: Array<out CharSequence> = emptyArray()
    var entryValues: Array<out CharSequence> = emptyArray()
    var value: String? = null
    fun findIndexOfValue(v: String?): Int = entryValues.indexOfFirst { it == v }
    override fun setDefaultValue(defaultValue: Any?) { if (value == null) value = defaultValue as? String }
}

class MultiSelectListPreference(context: android.content.Context = android.content.Context()) : DialogPreference(context) {
    var entries: Array<out CharSequence>? = null
    var entryValues: Array<out CharSequence>? = null
    var values: Set<String> = emptySet()
    override fun setDefaultValue(defaultValue: Any?) {
        if (values.isEmpty()) {
            @Suppress("UNCHECKED_CAST")
            values = (defaultValue as? Set<String>) ?: emptySet()
        }
    }
}

class PreferenceCategory(context: android.content.Context = android.content.Context()) : Preference(context) {
    private val _prefs = mutableListOf<Preference>()
    fun addPreference(preference: Preference): Boolean { _prefs.add(preference); return true }
    fun removeAll() { _prefs.clear() }
}

class PreferenceScreen(context: android.content.Context = android.content.Context()) : Preference(context) {
    private val _prefs = mutableListOf<Preference>()
    val preferenceCount: Int get() = _prefs.size
    fun addPreference(preference: Preference): Boolean { _prefs.add(preference); return true }
    fun getPreference(index: Int): Preference = _prefs[index]
    fun removeAll() { _prefs.clear() }
}
