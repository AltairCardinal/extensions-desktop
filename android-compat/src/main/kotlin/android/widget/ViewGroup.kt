package android.widget

import android.content.Context
import android.view.View

open class ViewGroup(context: Context = Context()) : View(context) {
    private val children = mutableListOf<View>()
    val childCount: Int get() = children.size

    fun addView(view: View) { children.add(view) }
    fun addView(view: View, index: Int) { children.add(index.coerceIn(0, children.size), view) }
    fun getChildAt(index: Int): View? = children.getOrNull(index)
    fun removeAllViews() { children.clear() }
    fun removeView(view: View) { children.remove(view) }
    fun removeViewAt(index: Int) { if (index in children.indices) children.removeAt(index) }
    fun indexOfChild(child: View): Int = children.indexOf(child)
}

class LinearLayout(context: Context = Context()) : ViewGroup(context) {
    var orientation: Int = VERTICAL
    companion object {
        const val HORIZONTAL = 0
        const val VERTICAL = 1
    }
}

class FrameLayout(context: Context = Context()) : ViewGroup(context)
class RelativeLayout(context: Context = Context()) : ViewGroup(context)
