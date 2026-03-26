package com.example.fairgo.domain.models

data class RideRequest(
    val fromLat: Double,
    val fromLng: Double,
    val toLat: Double,
    val toLng: Double,
)

data class Ride(
    val id: String,
    val status: RideStatus,
)

enum class RideStatus {
    Requested,
    Accepted,
    InProgress,
    Completed,
    Cancelled,
}

