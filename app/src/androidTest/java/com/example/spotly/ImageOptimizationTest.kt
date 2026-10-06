package com.example.spotly

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cloudinary.android.payload.FilePayload
import com.example.spotly.data.repository.postImagePreprocessing
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ImageOptimizationTest {
    @Test fun compressesWithoutChangingSourceAndPreservesOrientation() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val source = File(context.cacheDir, "optimization-source.jpg")
        val bitmap = Bitmap.createBitmap(2400, 1200, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(2400 * 1200) { i -> android.graphics.Color.rgb(i % 256, (i / 2400) % 256, (i * 17) % 256) }
        bitmap.setPixels(pixels, 0, 2400, 0, 0, 2400, 1200)
        source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
        bitmap.recycle()
        ExifInterface(source.path).apply {
            setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90.toString())
            saveAttributes()
        }
        val original = source.readBytes()
        assertEquals(6, ExifInterface(source.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, 0))
        val decoded = com.example.spotly.data.repository.OrientedBitmapDecoder(android.net.Uri.fromFile(source)).decode(context, FilePayload(source.path))
        assertTrue("Decodificación: ${decoded.width}x${decoded.height}", decoded.height > decoded.width)
        decoded.recycle()
        val result = File(context.filesDir, postImagePreprocessing(android.net.Uri.fromFile(source)).execute(context, FilePayload(source.path)))
        assertTrue("El SDK debe generar un JPEG válido", result.isFile && result.length() > 0)
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(result.path, options)
        assertTrue(options.outWidth <= 1600 && options.outHeight <= 1600)
        assertTrue("La orientación vertical debe conservarse: ${options.outWidth}x${options.outHeight}; ${result.path}", options.outHeight > options.outWidth)
        assertTrue("La foto debe pesar menos", result.length() < source.length())
        assertArrayEquals(original, source.readBytes())
        result.delete()
    }
}
