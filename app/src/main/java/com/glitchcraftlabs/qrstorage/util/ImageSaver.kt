package com.glitchcraftlabs.qrstorage.util

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.IOException
import java.io.OutputStream


fun ContentResolver.saveImage(
    bitmap: Bitmap,
    fileName: String,
    onSuccess: (Uri) -> Unit,
    onFailure: (e:IOException) -> Unit
){

    val relativePath = Environment.DIRECTORY_PICTURES + File.separator + "QRStorage"

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.ImageColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.ImageColumns.RELATIVE_PATH, relativePath)
        }
    }

    val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    var outputStream: OutputStream? = null
    try {

        val uri = this.insert(contentUri, contentValues)
            ?: throw IOException("Failed to create new MediaStore record.")

        outputStream = this.openOutputStream(uri)
            ?: throw IOException("Failed to create output stream")

        if (!bitmap.compress(Bitmap.CompressFormat.PNG, 95, outputStream)) {
            throw IOException("Failed to save bitmap.")
        }
        onSuccess(uri)
    } catch (e: IOException) {
        onFailure(e)
    } finally {
        outputStream?.close()
    }
}