package com.example.fairgo.domain.repository

import com.example.fairgo.domain.models.Ride
import com.example.fairgo.domain.models.RideRequest

interface RideRepository {
    suspend fun requestRide(request: RideRequest): Ride
}

