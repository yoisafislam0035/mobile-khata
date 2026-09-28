package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageStorageUtils {
    /**
     * Copies an image from content Uri (from Photo Picker or Camera temp file) to permanent local app storage.
     * Compresses to keep storage compact and performance smooth.
     * Returns permanent file:// URI string.
     */
    suspend fun saveImagePermanently(
        context: Context,
        sourceUri: Uri,
        prefix: String = "photo"
    ): String? = withContext(Dispatchers.IO) {
        try {
            val directory = File(context.filesDir, "mobi_khata_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = "${prefix}_${UUID.randomUUID()}.jpg"
            val destinationFile = File(directory, fileName)
            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            if (inputStream != null) {
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                if (originalBitmap != null) {
                    val maxDimension = 1280
                    val width = originalBitmap.width
                    val height = originalBitmap.height
                    val scaledBitmap = if (width > maxDimension || height > maxDimension) {
                        val ratio = width.toFloat() / height.toFloat()
                        val newWidth = if (width > height) maxDimension else (maxDimension * ratio).toInt()
                        val newHeight = if (height >= width) maxDimension else (maxDimension / ratio).toInt()
                        Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
                    } else {
                        originalBitmap
                    }
                    FileOutputStream(destinationFile).use { out ->
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                        out.flush()
                    }
                    if (scaledBitmap != originalBitmap) {
                        scaledBitmap.recycle()
                    }
                    originalBitmap.recycle()
                    Uri.fromFile(destinationFile).toString()
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Creates a temporary file and content Uri for capturing camera photos via FileProvider
     */
    fun createTempImageUri(context: Context, prefix: String = "camera_capture"): Pair<Uri, File>? {
        return try {
            val directory = File(context.cacheDir, "camera_temp")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File.createTempFile("${prefix}_", ".jpg", directory)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            Pair(uri, file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Safely deletes an existing saved file when user removes or replaces an image
     */
    suspend fun deleteImageIfExists(fileUriString: String?) = withContext(Dispatchers.IO) {
        if (!fileUriString.isNullOrBlank() && fileUriString.startsWith("file://")) {
            try {
                val file = File(Uri.parse(fileUriString).path ?: "")
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }
}
