package com.assisment.newschatprofileapp.utils



import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

// Convert Uri to Bitmap
fun Uri.toBitmap(context: android.content.Context): Bitmap? {
    return try {
        MediaStore.Images.Media.getBitmap(context.contentResolver, this)
    } catch (e: Exception) {
        null
    }
}

// Convert Bitmap to ImageBitmap
fun Bitmap.toImageBitmap(): ImageBitmap {
    return this.asImageBitmap()
}

// Save bitmap to file and return file path
fun Bitmap.saveToFile(context: android.content.Context, fileName: String): String {
    val file = File(context.filesDir, fileName)
    FileOutputStream(file).use { outputStream ->
        this.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
    }
    return file.absolutePath
}

// Load bitmap from file path
fun String.loadBitmapFromFile(): Bitmap? {
    return try {
        val file = File(this)
        if (file.exists()) {
            BitmapFactory.decodeFile(this)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

// Compress bitmap
fun Bitmap.compressBitmap(quality: Int = 80): Bitmap {
    val outputStream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
    val byteArray = outputStream.toByteArray()
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}

// Resize bitmap
fun Bitmap.resizeBitmap(width: Int, height: Int): Bitmap {
    return Bitmap.createScaledBitmap(this, width, height, true)
}