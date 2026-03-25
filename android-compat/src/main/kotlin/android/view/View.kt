package android.view

import android.content.Context

/** JVM stub for android.view.View. */
open class View(val context: Context = Context()) {
    var visibility: Int = VISIBLE
    val rootView: View get() = this

    @Suppress("UNCHECKED_CAST")
    fun <T : View> findViewById(id: Int): T? = null

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
