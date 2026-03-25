package android.view

import android.content.Context

/** JVM stub for android.view.View. */
open class View(val context: Context = Context()) {
    var visibility: Int = VISIBLE
    open val rootView: View get() = this

    open val parent: ViewGroup? get() = null

    @Suppress("UNCHECKED_CAST")
    fun <T> findViewById(id: Int): T? = null

    companion object {
        const val VISIBLE = 0
        const val INVISIBLE = 4
        const val GONE = 8
        const val LAYER_TYPE_HARDWARE = 2
        const val LAYER_TYPE_NONE = 0
    }

    fun setLayerType(layerType: Int, paint: Any?) {}
    fun addOnLayoutChangeListener(listener: Any?) {}
}
