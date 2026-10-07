package com.example.spotly.domain.repository

import com.example.spotly.domain.model.AppResult
import com.example.spotly.domain.model.LocationPoint

interface LocationRepository {
    suspend fun currentLocation(): AppResult<LocationPoint>
}
