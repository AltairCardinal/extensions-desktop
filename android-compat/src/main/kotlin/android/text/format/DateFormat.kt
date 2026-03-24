package android.text.format

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormat {
    @JvmStatic
    fun format(pattern: CharSequence, date: Date): CharSequence =
        SimpleDateFormat(pattern.toString(), Locale.getDefault()).format(date)

    @JvmStatic
    fun format(pattern: CharSequence, timeInMillis: Long): CharSequence =
        format(pattern, Date(timeInMillis))

    @JvmStatic
    fun getDateFormat(context: Context?): java.text.DateFormat =
        SimpleDateFormat.getDateInstance()

    @JvmStatic
    fun getLongDateFormat(context: Context?): java.text.DateFormat =
        SimpleDateFormat.getDateInstance(SimpleDateFormat.LONG)

    @JvmStatic
    fun getTimeFormat(context: Context?): java.text.DateFormat =
        SimpleDateFormat.getTimeInstance()
}
