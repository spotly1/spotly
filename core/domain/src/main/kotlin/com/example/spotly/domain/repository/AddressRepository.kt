package com.example.spotly.domain.repository

import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.LocationPoint
import java.util.Locale

interface AddressRepository {
    suspend fun resolve(point: LocationPoint, locale: Locale): AppResult<String>
}
