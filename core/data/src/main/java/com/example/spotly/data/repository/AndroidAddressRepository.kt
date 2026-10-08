package com.example.spotly.data.repository

import com.example.spotly.domain.repository.AddressRepository
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.AppError
import com.example.spotly.database.AddressCache

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.example.spotly.domain.model.LocationPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

/** Traduce puntos públicos; no solicita permisos ni consulta la ubicación del dispositivo. */
class AndroidAddressRepository(context: Context) : AddressRepository {
    private val context = context.applicationContext

    override suspend fun resolve(point: LocationPoint, locale: Locale): AppResult<String> {
        val key = "${locale.toLanguageTag()}:${point.latitude}:${point.longitude}"
        cache.get(key)?.let { return AppResult.Success(it) }
        if (!Geocoder.isPresent()) return AppResult.Error(AppError.AddressUnavailable)
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
            val shortAddress = address?.let {
                val city = it.locality ?: it.subAdminArea
                val region = it.adminArea

                listOfNotNull(city, region)
                    .filter { part -> part.isNotBlank() }
                    .distinct()
                    .joinToString(", ")
                    .ifBlank { it.countryName ?: "" }
            }

            shortAddress?.takeIf { it.isNotBlank() }
                ?.also { cache.put(key, it) }
                ?.let { AppResult.Success(it) }
                ?: AppResult.Error(AppError.AddressUnavailable)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            AppResult.Error(AppError.AddressUnavailable)
        }
    }

    companion object {
        // Acotada y solo en memoria: evita repetir búsquedas al pasar del feed al perfil.
        private val cache = AddressCache(100)
    }
}
