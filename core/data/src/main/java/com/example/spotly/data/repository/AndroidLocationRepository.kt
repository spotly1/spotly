package com.example.spotly.data.repository

import com.example.spotly.domain.repository.LocationRepository
import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.AppError
import com.example.spotly.domain.model.LocationPoint
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class AndroidLocationRepository(context: Context) : LocationRepository {
    private val appContext = context.applicationContext

    override suspend fun currentLocation(): AppResult<LocationPoint> {
        return try {
            val point = findLocation()
            if (point == null) AppResult.Error(AppError.LocationUnavailable)
            else AppResult.Success(point)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            AppResult.Error(AppError.LocationUnavailable)
        }
    }

    private suspend fun findLocation(): LocationPoint? {
        val hasPermission = ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return null
        val manager = appContext.getSystemService(LocationManager::class.java)
        if (!LocationManagerCompat.isLocationEnabled(manager)) return null
        val provider = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .firstOrNull { manager.isProviderEnabled(it) } ?: return null
        return withTimeoutOrNull(20_000) {
            suspendCancellableCoroutine { continuation ->
                val cancellation = CancellationSignal()
                continuation.invokeOnCancellation { cancellation.cancel() }
                LocationManagerCompat.getCurrentLocation(manager, provider, cancellation,
                    ContextCompat.getMainExecutor(appContext)) { location ->
                    if (continuation.isActive) continuation.resume(location?.let { LocationPoint(it.latitude, it.longitude) })
                }
            }
        }
    }
}
