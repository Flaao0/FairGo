package com.example.fairgo.data.network.models

import com.google.gson.annotations.SerializedName

data class RideResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("price")
    val price: String,
)

