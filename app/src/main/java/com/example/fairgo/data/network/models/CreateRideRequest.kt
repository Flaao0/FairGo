package com.example.fairgo.data.network.models

import com.google.gson.annotations.SerializedName

data class CreateRideRequest(
    @SerializedName("start_lat")
    val startLat: Double,
    @SerializedName("start_lon")
    val startLon: Double,
    @SerializedName("finish_lat")
    val finishLat: Double,
    @SerializedName("finish_lon")
    val finishLon: Double,
)

