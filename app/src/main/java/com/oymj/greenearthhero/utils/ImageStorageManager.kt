package com.oymj.greenearthhero.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.File
import java.io.FileInputStream

class ImageStorageManager {
    companion object {
        fun saveImgToInternalStorage(context: Context, bitmapImage: Bitmap, imageFileName: String,callback:(absolutePath:String)->Unit) {
            GlobalScope.async{
                context.openFileOutput(imageFileName, Context.MODE_PRIVATE).use { outputStream ->
                    bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
                callback(context.filesDir.absolutePath)
            }
        }

        fun getImgFromInternalStorage(context: Context, imageFileName: String): Bitmap? {
            val directory = context.filesDir
            val file = File(directory, imageFileName)
            return BitmapFactory.decodeStream(FileInputStream(file))
        }

        fun deleteImgFromInternalStorage(context: Context, imageFileName: String): Boolean {
            val dir = context.filesDir
            val file = File(dir, imageFileName)
            return file.delete()
        }
    }
}