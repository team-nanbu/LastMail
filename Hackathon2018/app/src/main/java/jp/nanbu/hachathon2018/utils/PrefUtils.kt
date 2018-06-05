package jp.nanbu.hachathon2018.utils

import android.content.Context
import android.content.SharedPreferences

class PrefUtils(private val context: Context) {
    companion object {
        private const val PREF_NAME = "application_preference"
        const val SENDER_MAIL_ADDRESS = "sender_mail_address"
        const val SENDER_NAME = "sender_name"
    }


    /**
     * String型をプリファレンスに保存
     */
    fun put(key: String, value: String) {
        writePref().putString(key, value).apply()
    }

    /**
     * int型をプリファレンスに保存
     */
    fun put(key: String, value: Int) {
        writePref().putInt(key, value).apply()
    }

    /**
     * boolean型をプリファレンスに保存
     */
    fun put(key: String, value: Boolean) {
        writePref().putBoolean(key, value).apply()
    }

    /**
     * float型をプリファレンスに保存
     */
    fun put(key: String, value: Float) {
        writePref().putFloat(key, value).apply()
    }

    /**
     * long型をプリファレンスに保存
     */
    fun put(key: String, value: Long) {
        writePref().putLong(key, value).apply()
    }

    /**
     * String型を取得
     */
    fun getPrefString(key: String, defaultVal: String?): String? = readPref().getString(key, defaultVal)


    /**
     * int型を取得
     */
    fun getPrefInt(key: String, defaultVal: Int): Int = readPref().getInt(key, defaultVal)


    /**
     * boolean型を取得
     */
    fun getPrefBool(key: String, defaultVal: Boolean): Boolean = readPref().getBoolean(key, defaultVal)


    /**
     * float型を取得
     */
    fun getPrefFloat(key: String, defaultVal: Float): Float = readPref().getFloat(key, defaultVal)


    /**
     * long型をプリファレンスから取得
     */
    fun getPrefLong(key: String, defaultVal: Long): Long = readPref().getLong(key, defaultVal)


    /**
     * プリファレンス取得
     */
    private fun readPref(): SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * プリファレンス書き込みEditor取得
     */
    private fun writePref(): SharedPreferences.Editor {
        return readPref().edit()
    }


}