package com.example.spotly.domain.usecase

import com.example.spotly.domain.repository.LocationRepository

class GetCurrentLocationUseCase(private val repository: LocationRepository) {
    suspend operator fun invoke() = repository.currentLocation()
}
