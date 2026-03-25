package android.view

import android.content.Context

open class ViewGroup(context: Context = Context()) : View(context) {
    protected val children = mutableListOf<View>()
    val childCount: Int get() = children.size

    fun addView(view: View) { children.add(view) }
    fun addView(view: View, index: Int) { children.add(index.coerceIn(0, children.size), view) }
    fun removeView(view: View) { children.remove(view) }
    fun removeAllViews() { children.clear() }
    fun getChildAt(index: Int): View? = children.getOrNull(index)
}
