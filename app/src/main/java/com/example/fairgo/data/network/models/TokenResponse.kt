package com.example.fairgo.data.network.models

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("access")
    val access: String,
    @SerializedName("refresh")
    val refresh: String
)
