package com.example.fairgo.data.network

import com.google.gson.annotations.SerializedName

data class RideStatusMessage(
    @SerializedName("ride_id")
    val rideId: Int,
    @SerializedName("status")
    val status: String,
)

