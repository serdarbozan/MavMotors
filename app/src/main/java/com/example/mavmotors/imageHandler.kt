package com.example.mavmotors

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

object ImageStorageHelper {

    // Save image from URI to internal app storage
    fun saveImageToInternalStorage(context: Context, imageUri: Uri): String? {
        return try {
            // Create a unique filename
            val filename = "vehicle_${UUID.randomUUID()}.jpg"

            // Open input stream from the URI
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Save to internal storage (private to your app)
            val file = File(context.filesDir, "vehicle_images")
            if (!file.exists()) {
                file.mkdirs()
            }

            val imageFile = File(file, filename)
            val outputStream = FileOutputStream(imageFile)

            // Compress and save (adjust quality as needed)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)

            outputStream.flush()
            outputStream.close()
            inputStream?.close()

            // Return the absolute path to the saved file
            imageFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    // Load bitmap from stored path
    fun loadImageFromPath(path: String): Bitmap? {
        return try {
            val file = File(path)
            if (file.exists()) {
                BitmapFactory.decodeFile(path)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Delete image when vehicle is deleted
    fun deleteImage(path: String): Boolean {
        return try {
            val file = File(path)
            file.delete()
        } catch (e: Exception) {
            false
        }
    }
}