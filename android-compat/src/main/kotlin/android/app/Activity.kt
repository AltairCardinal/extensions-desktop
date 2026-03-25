package android.app

import android.content.Context
import android.content.Intent
import android.os.Bundle

open class Activity : Context() {
    val intent: Intent = Intent()
    override val packageName: String = ""

    open fun onCreate(savedInstanceState: Bundle?) {}
    open fun finish() {}
    fun startActivity(intent: Intent?) {}
}
