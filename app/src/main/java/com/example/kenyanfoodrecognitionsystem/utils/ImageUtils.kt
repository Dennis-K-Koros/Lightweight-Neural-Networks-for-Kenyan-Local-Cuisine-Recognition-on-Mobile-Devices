package com.example.kenyanfoodrecognitionsystem.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility functions for image handling in meal history
 */
object ImageUtils {

    /**
     * Convert Bitmap to Base64 string (for small images)
     * Use this for storing images directly in the database
     */
    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 50): String {
        return ByteArrayOutputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        }
    }

    /**
     * Convert Base64 string back to Bitmap
     */
    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Save bitmap to a file in the app's **permanent internal storage** and return the path.
     */
    fun saveBitmapToFile(context: Context, bitmap: Bitmap): String? {
        // 1. Create a unique, timestamp-based filename
        val timestamp = System.currentTimeMillis()
        val fileName = "meal_image_$timestamp.jpg"

        // 2. Define the file location in the app's internal files directory (permanent)
        val file = File(context.filesDir, fileName)

        return try {
            // Use 'use' to ensure FileOutputStream is properly closed
            FileOutputStream(file).use { out ->
                // Compress the bitmap and write it to the file stream
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                out.flush() // Ensure all data is written
            }
            // Return the absolute path, which is permanently saved in the Room DB (MealHistory)
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Load Bitmap from file path
     */
    fun loadBitmapFromFile(filePath: String): Bitmap? {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(filePath)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Delete image file from storage
     */
    fun deleteImageFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get current date string in format "yyyy-MM-dd"
     */
    fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    /**
     * Get current timestamp in milliseconds
     */
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }

    /**
     * Format timestamp to readable date/time
     */
    fun formatTimestamp(timestamp: Long, pattern: String = "MMM dd, yyyy HH:mm"): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
    }

    /**
     * Resize bitmap to reduce size before saving
     */
    fun resizeBitmap(bitmap: Bitmap, maxWidth: Int = 800, maxHeight: Int = 800): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val ratio = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}