package android.text

/** JVM stub for android.text.TextWatcher. */
interface TextWatcher {
    fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
    fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
    fun afterTextChanged(editable: Editable?)
}

/** JVM stub for android.text.Editable. */
interface Editable : CharSequence {
    override fun toString(): String
}
