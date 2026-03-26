package com.example.fairgo.domain.usecases

import com.example.fairgo.domain.models.Ride
import com.example.fairgo.domain.models.RideRequest
import com.example.fairgo.domain.repository.RideRepository

class RequestRideUseCase(
    private val rideRepository: RideRepository,
) {
    suspend operator fun invoke(request: RideRequest): Ride = rideRepository.requestRide(request)
}

