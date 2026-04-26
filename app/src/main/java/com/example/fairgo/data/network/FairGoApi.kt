package com.example.fairgo.data.network

import com.example.fairgo.data.network.models.AuthRequest
import com.example.fairgo.data.network.models.CreateRideRequest
import com.example.fairgo.data.network.models.RegisterRequest
import com.example.fairgo.data.network.models.RideResponse
import com.example.fairgo.data.network.models.TokenResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface FairGoApi {

    @POST("users/register/")
    suspend fun register(@Body request: RegisterRequest)

    @POST("token/")
    suspend fun login(@Body request: AuthRequest): TokenResponse

    @POST("rides/")
    suspend fun createRide(@Body request: CreateRideRequest): RideResponse
}
