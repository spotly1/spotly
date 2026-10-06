package com.example.spotly.data.repository

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.LruCache
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

/** Traduce puntos públicos; no solicita permisos ni consulta la ubicación del dispositivo. */
class AddressRepository(context: Context) {
    private val context = context.applicationContext

    suspend fun resolve(point: GeoPoint, locale: Locale): String? {
        val key = "${locale.toLanguageTag()}:${point.latitude}:${point.longitude}"
        cache.get(key)?.let { return it }
        if (!Geocoder.isPresent()) return null
        return try {
            val address = withTimeoutOrNull(8_000) {
                val geocoder = Geocoder(context, locale)
                if (Build.VERSION.SDK_INT >= 33) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(point.latitude, point.longitude, 1,
                            object : Geocoder.GeocodeListener {
                                override fun onGeocode(addresses: MutableList<Address>) {
                                    if (continuation.isActive) continuation.resume(addresses.firstOrNull())
                                }
                                override fun onError(errorMessage: String?) {
                                    if (continuation.isActive) continuation.resume(null)
                                }
                            })
                    }
                } else withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(point.latitude, point.longitude, 1)?.firstOrNull()
                }
            }
            address?.getAddressLine(0)?.trim()?.takeIf { it.isNotEmpty() }
                ?.also { cache.put(key, it) }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        // Acotada y solo en memoria: evita repetir búsquedas al pasar del feed al perfil.
        private val cache = LruCache<String, String>(100)
    }
}
