package android.widget

import android.content.Context
import android.view.View

open class TextView(context: Context = Context()) : View(context) {
    var text: CharSequence? = null
    var textSize: Float = 14f
    var movementMethod: android.text.method.MovementMethod? = null
    var error: CharSequence? = null
    var gravity: Int = 0
    var maxLines: Int = Int.MAX_VALUE
    var ellipsize: android.text.TextUtils.TruncateAt? = null

    fun setTextColor(color: Int) {}
    fun setTextSize(unit: Int, size: Float) { textSize = size }
    fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {}
    fun setHint(hint: CharSequence?) {}
    fun setHint(resId: Int) {}
    fun setLines(lines: Int) {}
    fun setSingleLine(singleLine: Boolean) {}
    fun addTextChangedListener(watcher: android.text.TextWatcher?) {}
    fun removeTextChangedListener(watcher: android.text.TextWatcher?) {}
    fun setText(resId: Int) {}
}

open class EditText(context: Context = Context()) : TextView(context) {
    var inputType: Int = 0
    fun setSelection(index: Int) {}
    fun setSelection(start: Int, stop: Int) {}
}
