package android.content

import android.net.Uri
import android.os.Bundle

open class Intent {
    var action: String? = null
    var data: Uri? = null
    var type: String? = null
    private val extras = Bundle()

    constructor()
    constructor(action: String?) {
        this.action = action
    }
    constructor(action: String?, uri: Uri?) {
        this.action = action
        this.data = uri
    }

    fun setData(data: Uri?): Intent { this.data = data; return this }
    fun setAction(action: String?): Intent { this.action = action; return this }
    fun setType(type: String?): Intent { this.type = type; return this }
    fun putExtra(name: String, value: String?): Intent { return this }
    fun putExtra(name: String, value: Boolean): Intent { return this }
    fun putExtra(name: String, value: Int): Intent { return this }
    fun addFlags(flags: Int): Intent { return this }
    fun addCategory(category: String): Intent { return this }

    companion object {
        const val ACTION_VIEW = "android.intent.action.VIEW"
        const val FLAG_ACTIVITY_NEW_TASK = 0x10000000
    }
}
