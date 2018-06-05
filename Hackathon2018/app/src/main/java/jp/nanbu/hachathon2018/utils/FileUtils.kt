package jp.nanbu.hachathon2018.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.File

object FileUtils {
    fun getVideoFileFromUri(context: Context, uri: Uri): File? {
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            var path: String? = null
            if (cursor.moveToFirst()) {
                path = cursor.getString(0)
            }
            cursor.close()
            if (path != null) {
                return File(path)
            }
        }
        return null
    }
}