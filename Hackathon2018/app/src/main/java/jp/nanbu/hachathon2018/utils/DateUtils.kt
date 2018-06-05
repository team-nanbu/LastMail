package jp.nanbu.hachathon2018.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun getTimeStamp(): String {
        val date = Date()
        val format = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        return format.format(date)
    }

}